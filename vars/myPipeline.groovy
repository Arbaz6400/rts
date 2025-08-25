def call() {
    pipeline {
        agent any

        environment {
            GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Versioning') {
                steps {
                    script {
                        // Read build.gradle from repo
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")

                        // Match: version = '1.0.0' or version = "1.0.0"
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME in ["main", "stg"]) {
                            newVersion = baseVersion
                        }

                        env.PROJECT_VERSION = newVersion
                        echo "📌 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh """
                            ./gradlew clean build \
                              -Pversion=${env.PROJECT_VERSION} \
                              -PNEXUS_USERNAME=$NEXUS_USERNAME \
                              -PNEXUS_PASSWORD=$NEXUS_PASSWORD
                        """
                    }
                }
            }

            stage('Publish') {
                when {
                    expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'release' || env.BRANCH_NAME == 'stg' }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        sh """
                            ./gradlew publish \
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
                echo "✅ Build and publish succeeded for version ${env.PROJECT_VERSION}"
            }
            failure {
                echo "❌ Build or publish failed."
            }
        }
    }
}
