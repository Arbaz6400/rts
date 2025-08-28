def call(String branchName = env.BRANCH_NAME) {
    pipeline {
        agent any

        environment {
            APP_VERSION = ''
        }

        stages {
            stage('Prepare') {
                steps {
                    script {
                        // Determine app version based on branch
                        APP_VERSION = getVersionForBranch(branchName ?: 'main')
                        echo "Using version: ${APP_VERSION}"
                    }
                }
            }

            stage('Checkout') {
                steps {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${branchName ?: 'main'}"]],
                        userRemoteConfigs: [[
                            url: 'https://github.com/Arbaz6400/Streaming.git',
                            credentialsId: 'b53cd150-f2f4-404d-9ea6-92bcab9138b9'
                        ]]
                    ])
                }
            }

            stage('Build') {
                steps {
                    script {
                        if (isUnix()) {
                            sh "./gradlew clean shadowJar -PappVersion=${APP_VERSION}"
                        } else {
                            bat "gradlew.bat clean shadowJar -PappVersion=${APP_VERSION}"
                        }
                    }
                }
            }

            stage('Upload') {
                steps {
                    echo "Upload stage skipped (configure Nexus if needed)"
                    // Optional: uncomment Nexus upload steps if required
                }
            }
        }

        post {
            success {
                echo "Build succeeded with version ${APP_VERSION}"
            }
            failure {
                echo "Build failed"
            }
        }
    }
}

// Helper function
def getVersionForBranch(branchName) {
    if (branchName == 'main') {
        return "1.0.0-main"
    } else if (branchName == 'develop') {
        return "1.0.0-dev"
    } else {
        return "1.0.0-${branchName}"
    }
}
