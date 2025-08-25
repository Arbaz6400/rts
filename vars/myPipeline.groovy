def call() {
    pipeline {
        agent any

        tools {
            gradle 'Gradle-8.3' // optional fallback if gradlew.bat is missing
        }

        environment {
            PROJECT_VERSION = ""
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
                        // Read build.gradle version
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
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
                        script {
                            def gradlewExists = fileExists("${env.WORKSPACE}/gradlew.bat")
                            if (gradlewExists) {
                                bat """
                                    gradlew.bat clean build ^
                                      -Pversion=${env.PROJECT_VERSION} ^
                                      -PNEXUS_USERNAME=${env.NEXUS_USERNAME} ^
                                      -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}
                                """
                            } else {
                                echo "⚠️ gradlew.bat not found. Using Jenkins Gradle tool."
                                bat """
                                    gradle clean build ^
                                      -Pversion=${env.PROJECT_VERSION} ^
                                      -PNEXUS_USERNAME=${env.NEXUS_USERNAME} ^
                                      -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}
                                """
                            }
                        }
                    }
                }
            }

            stage('Publish') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        script {
                            def gradlewExists = fileExists("${env.WORKSPACE}/gradlew.bat")
                            if (gradlewExists) {
                                bat """
                                    gradlew.bat publish ^
                                      -Pversion=${env.PROJECT_VERSION} ^
                                      -PNEXUS_USERNAME=${env.NEXUS_USERNAME} ^
                                      -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}
                                """
                            } else {
                                echo "⚠️ gradlew.bat not found. Using Jenkins Gradle tool."
                                bat """
                                    gradle publish ^
                                      -Pversion=${env.PROJECT_VERSION} ^
                                      -PNEXUS_USERNAME=${env.NEXUS_USERNAME} ^
                                      -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}
                                """
                            }
                        }
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build and publish completed successfully!"
            }
            failure {
                echo "❌ Build or publish failed."
            }
        }
    }
}
