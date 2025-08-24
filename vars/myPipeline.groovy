def call() {
    pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Versioning') {
            steps {
                script {
                    def gradleVersion = sh(
                        script: "grep '^version' build.gradle | awk '{print \$3}' | tr -d \"'\"",
                        returnStdout: true
                    ).trim()

                    def baseVersion = gradleVersion ?: "0.0.1"
                    def branchName = env.BRANCH_NAME ?: 'develop'

                    if (branchName == "main") {
                        newVersion = baseVersion
                    } else if (branchName == "release") {
                        newVersion = baseVersion + "-RC"
                    } else if (branchName == "develop") {
                        newVersion = baseVersion + "-SNAPSHOT"
                    } else {
                        newVersion = baseVersion + "-${branchName}"
                    }

                    echo "📌 Using version: ${newVersion}"
                }
            }
        }

        
    }
}
}
