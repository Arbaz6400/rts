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
                        // Read build.gradle from Streaming repo
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based suffix
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
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
                            if (fileExists("${env.WORKSPACE}/gradlew.bat")) {
                                echo "⚡ Using Gradle wrapper"
                                bat """
                                    gradlew.bat clean build ^
                                      -Pversion=%PROJECT_VERSION% ^
                                      -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                      -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
                                """
                            } else {
                                echo "⚡ Gradle wrapper not found. Using Jenkins Gradle tool"
                                def gradleHome = tool name: 'Gradle-8.3', type: 'gradle'
                                bat """
                                    "${gradleHome}\\bin\\gradle.bat" clean build ^
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
                            def gradleCmd = fileExists("${env.WORKSPACE}/gradlew.bat") ? "gradlew.bat" : "${tool name: 'Gradle-8.3', type: 'gradle'}\\bin\\gradle.bat"
                            bat """
                                ${gradleCmd} publish ^
                                  -Pversion=%PROJECT_VERSION% ^
                                  -PNEXUS_USERNAME=%NEXUS_USERNAME% ^
                                  -PNEXUS_PASSWORD=%NEXUS_PASSWORD%
                            """
                        }
                    }
                }
            }
        }

        post {
            success { echo "✅ Build and publish completed successfully!" }
            failure { echo "❌ Build or publish failed." }
        }
    }
}
