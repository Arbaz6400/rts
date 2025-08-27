import org.rts.utils.VersionUtils

def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        def versionUtils = new VersionUtils(this)
                        def baseVersion = versionUtils.getProjectVersion()

                        echo "📖 Base version: ${baseVersion}"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            newVersion = baseVersion
                        }

                        env.APP_VERSION = newVersion
                        echo "📌 Final version: ${env.APP_VERSION}"
                    }
                }
            }

            stage('Upload to Nexus') {
            steps {
                script {
                    // Define Nexus-specific variables here
                    def nexusRepo   = "releases"   // Your Nexus repository ID
                    def pomFile     = "build/publications/mavenJava/pom-default.xml"
                    def shadowJar   = true          // true if using Shadow JAR

                    // Create NexusRest instance using the pipeline steps
                    def nexusRest = new org.enbd.common.NexusRest(this)

                    echo "Uploading Shadow JAR to Nexus..."

                    nexusRest.uploadReleaseProdNexus(
                        pomFile,
                        nexusRepo,
                        shadowJar
                    )

                    echo "Upload completed."
                }
            }
            }
