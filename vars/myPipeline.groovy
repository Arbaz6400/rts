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

            stage('Build & Upload') {
                steps {
                    script {
                        echo "⬆️ Uploading ${env.APP_VERSION} to Nexus..."
                        // nexusUpload(...)   // your upload logic
                    }
                }
            }
        }
    }
}
