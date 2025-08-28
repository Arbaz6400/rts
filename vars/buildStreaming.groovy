def call() {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    script {
                        // Compute version inside script block
                        def appVersion = getVersionForBranch(env.BRANCH_NAME)
                        env.APP_VERSION = appVersion
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
            success { echo "Build and upload completed successfully." }
            failure { echo "Build or upload failed." }
        }
    }
}

// Closure to compute version
def getVersionForBranch = { branch ->
    if (branch == 'master') {
        return "1.0.0"
    } else if (branch.startsWith('release/')) {
        return branch.replace('release/', '') + "-RC"
    } else {
        return "1.0.0-${branch}"
    }
}
