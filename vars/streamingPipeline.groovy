// streamingPipeline.groovy
def call() {
    pipeline {
        agent any

        environment {
            APP_VERSION = ''
        }

        stages {
            stage('Checkout SCM') {
                steps {
                    checkout scm
                }
            }

            stage('Versioning') {
                steps {
                    script {
                        // Example: compute version (you can replace this with actual logic)
                        def version = "1.0.0-SNAPSHOT"
                        env.APP_VERSION = version
                        echo "📌 Computed version: ${version}"
                    }
                }
            }

            stage('Upload to Nexus') {
                steps {
                    script {
                        // Only pass version + repo
                        nexusUpload(env.APP_VERSION, "prod-repo")
                    }
                }
            }
        }
    }
}
