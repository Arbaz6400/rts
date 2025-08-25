def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            GRADLE_USER_HOME = "${env.WORKSPACE}/.gradle"
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Set Version') {
                steps {
                    script {
                        def baseVersion = "1.0.0"  // fallback, can also be from gradle.properties
                        if (env.BRANCH_NAME == "main") {
                            env.PROJECT_VERSION = baseVersion
                        } else if (env.BRANCH_NAME == "develop") {
                            env.PROJECT_VERSION = "${baseVersion}-SNAPSHOT"
                        } else {
                            // feature/my-feature → 1.0.0-my-feature
                            def safeBranch = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9.-]', '-')
                            env.PROJECT_VERSION = "${baseVersion}-${safeBranch}"
                        }
                        echo "📦 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh """
                            gradle7 -g ${env.GRADLE_USER_HOME} --stacktrace --no-daemon clean build \
                              -Pversion=${env.PROJECT_VERSION} \
                              -PNEXUS_USERNAME=$NEXUS_USERNAME \
                              -PNEXUS_PASSWORD=$NEXUS_PASSWORD
                        """
                    }
                }
            }

            stage('Publish') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh """
                            gradle7 -g ${env.GRADLE_USER_HOME} --stacktrace --no-daemon publish \
                              -Pversion=${env.PROJECT_VERSION} \
                              -PNEXUS_USERNAME=$NEXUS_USERNAME \
                              -PNEXUS_PASSWORD=$NEXUS_PASSWORD
                        """
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build and publish completed successfully. Version: ${env.PROJECT_VERSION}"
            }
            failure {
                echo "❌ Build or publish failed."
            }
        }
    }
}
