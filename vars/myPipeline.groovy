def call() {
    pipeline {
        agent any

        environment {
            PROJECT_VERSION = ""
        }

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Check if build.gradle exists
                        def gradleFilePath = "${env.WORKSPACE}/build.gradle"
                        if (!fileExists(gradleFilePath)) {
                            error "build.gradle not found at ${gradleFilePath}"
                        }

                        // Read build.gradle
                        def gradleFile = readFile(gradleFilePath)
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Detect branch
                        def branchName = env.BRANCH_NAME ?: "unknown"
                        echo "🔖 Branch detected: ${branchName}"

                        // Determine version based on branch
                        def newVersion = baseVersion
                        if (branchName == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (branchName == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (branchName in ["main", "stg"]) {
                            newVersion = baseVersion
                        } else {
                            newVersion = "${baseVersion}-DEV"
                        }

                        // Save for downstream stages
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
                            def gradlewPath = "${env.WORKSPACE}/gradlew.bat"
                            if (fileExists(gradlewPath)) {
                                echo "⚡ Using Gradle wrapper"
                                bat """
                                    gradlew.bat clean build ^
                                        -Pversion=%PROJECT_VERSION% ^
                                        -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                        -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
                                """
                            } else {
                                echo "⚠️ Gradle wrapper not found. Using Jenkins Gradle tool"
                                def gradleHome = tool name: 'Gradle-8.3', type: 'gradle'
                                bat """
                                    "${gradleHome}/bin/gradle.bat" clean build ^
                                        -Pversion=%PROJECT_VERSION% ^
                                        -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                        -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
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
                            def gradlewPath = "${env.WORKSPACE}/gradlew.bat"
                            if (fileExists(gradlewPath)) {
                                bat """
                                    gradlew.bat publish ^
                                        -Pversion=%PROJECT_VERSION% ^
                                        -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                        -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
                                """
                            } else {
                                def gradleHome = tool name: 'Gradle-8.3', type: 'gradle'
                                bat """
                                    "${gradleHome}/bin/gradle.bat" publish ^
                                        -Pversion=%PROJECT_VERSION% ^
                                        -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                        -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
                                """
                            }
                        }
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build and publish completed successfully! Version: ${env.PROJECT_VERSION}"
            }
            failure {
                echo "❌ Build or publish failed."
            }
        }
    }
}
