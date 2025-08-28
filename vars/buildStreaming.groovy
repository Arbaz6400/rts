// vars/buildStreaming.groovy

def call() {
    pipeline {
        agent any

        environment {
            // Compute version dynamically using closure
            APP_VERSION = getVersionForBranch(env.BRANCH_NAME)
        }

        stages {
            stage('Checkout') {
                steps {
                    echo "Checking out branch ${env.BRANCH_NAME}"
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "Building streaming JAR with version: ${env.APP_VERSION}"
                        // Run Gradle shadowJar with version
                        sh "./gradlew clean shadowJar -PappVersion=${env.APP_VERSION}"
                    }
                }
            }

            stage('Upload') {
                steps {
                    script {
                        def jarName = "streaming-${env.APP_VERSION}-all.jar"
                        echo "Uploading JAR: ${jarName}"
                        // Replace below with your actual upload logic
                        sh """
                        curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD \
                        -T build/libs/${jarName} \
                        https://nexus.example.com/repository/releases/
                        """
                    }
                }
            }
        }

        post {
            success {
                echo "Build and upload completed successfully."
            }
            failure {
                echo "Build or upload failed."
            }
        }
    }
}

// Closure to compute version based on branch
def getVersionForBranch = { branch ->
    if (branch == 'master') {
        return "1.0.0"
    } else if (branch.startsWith('release/')) {
        return branch.replace('release/', '') + "-RC"
    } else {
        return "1.0.0-${branch}"
    }
}
