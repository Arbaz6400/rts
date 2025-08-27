// vars/test.groovy
def call() {
    pipeline {
        agent any

        stages {
            stage('Compute Version') {
                steps {
                    script {
                        def baseVersion = "1.0.0"
                        def branchName = env.BRANCH_NAME.replaceAll("/", "-")
                        env.APP_VERSION = "${baseVersion}-${branchName}"
                        echo "Computed APP_VERSION = ${env.APP_VERSION}"
                    }
                }
            }

            stage('Prepare POM') {
                steps {
                    script {
                        // Read original pom.xml
                        def pomPath = "pom.xml"
                        def pomContent = readFile(pomPath)

                        echo "====== Original pom.xml ======"
                        echo pomContent
                        echo "=============================="

                        // Replace version with APP_VERSION
                        def updatedPom = pomContent.replaceAll(/<version>.*<\/version>/, "<version>${env.APP_VERSION}</version>")

                        // Write updated pom to pom-temp.xml
                        writeFile(file: "pom-temp.xml", text: updatedPom)

                        echo "====== Updated pom-temp.xml ======"
                        echo updatedPom
                        echo "=================================="

                        // Extract version from updated pom
                        def versionMatch = updatedPom =~ /<version>(.+)<\/version>/
                        echo "Version in temporary POM: ${versionMatch[0][1]}"
                    }
                }
            }

            stage('Upload Artifact') {
                steps {
                    script {
                        def nexusRest = new org.enbd.common.NexusRest(this)
                        nexusRest.uploadReleaseProdNexus('pom-temp.xml', 'my-release-repo', true)
                    }
                }
            }
        }
    }
}
