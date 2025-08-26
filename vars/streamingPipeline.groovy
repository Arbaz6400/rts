def call(String pomFile = 'pom.xml', String nexusRepo = 'prod-repo') {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Read build.gradle in the workspace
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        }

                        echo "📌 Computed version: ${newVersion}"
                        env.APP_VERSION = newVersion
                    }
                }
            }

            stage('Upload to Nexus') {
                steps {
                    script {
                        nexusUpload(pomFile, nexusRepo, true, env.APP_VERSION)
                    }
                }
            }
        }
    }
}
