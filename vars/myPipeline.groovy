def call() {
    pipeline {
        agent any

        environment {
            // Gradle tool name configured in Jenkins (optional if wrapper exists)
            GRADLE_TOOL = "Gradle-8.3"
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
                        def gradleFilePath = "${env.WORKSPACE}/build.gradle"
                        if (!fileExists(gradleFilePath)) {
                            error "❌ build.gradle not found in workspace!"
                        }

                        def gradleFile = readFile(gradleFilePath)
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based suffix logic
                        if (env.BRANCH_NAME == "develop") {
                            env.NEW_VERSION = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            env.NEW_VERSION = "${baseVersion}-RC"
                        } else {
                            env.NEW_VERSION = baseVersion
                        }

                        echo "📌 Using version: ${env.NEW_VERSION}"
                    }
                }
            }

            stage('Check Credentials') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus-creds', 
                        usernameVariable: 'NEXUS_USERNAME', 
                        passwordVariable: 'NEXUS_PASSWORD'
                    )]) {
                        script {
                            if (!env.NEXUS_USERNAME || !env.NEXUS_PASSWORD) {
                                error "❌ Nexus credentials not loaded!"
                            } else {
                                echo "✅ Nexus credentials loaded"
                            }
                        }
                    }
                }
            }

            stage('Build') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus-creds', 
                        usernameVariable: 'NEXUS_USERNAME', 
                        passwordVariable: 'NEXUS_PASSWORD'
                    )]) {
                        script {
                            // Use Gradle wrapper if present, otherwise Jenkins tool
                            def gradleCmd = fileExists("${env.WORKSPACE}/gradlew.bat") ? 
                                "${env.WORKSPACE}/gradlew.bat" : 
                                "${tool(env.GRADLE_TOOL)}/bin/gradle.bat"

                            bat "\"${gradleCmd}\" clean build -Pversion=${env.NEW_VERSION} -PNEXUS_USERNAME=%NEXUS_USERNAME% -PNEXUS_PASSWORD=%NEXUS_PASSWORD%"
                        }
                    }
                }
            }

            stage('Publish') {
                steps {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus-creds', 
                        usernameVariable: 'NEXUS_USERNAME', 
                        passwordVariable: 'NEXUS_PASSWORD'
                    )]) {
                        script {
                            def gradleCmd = fileExists("${env.WORKSPACE}/gradlew.bat") ? 
                                "${env.WORKSPACE}/gradlew.bat" : 
                                "${tool(env.GRADLE_TOOL)}/bin/gradle.bat"

                            bat "\"${gradleCmd}\" publish -Pversion=${env.NEW_VERSION} -PNEXUS_USERNAME=%NEXUS_USERNAME% -PNEXUS_PASSWORD=%NEXUS_PASSWORD%"
                        }
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build and publish succeeded!"
            }
            failure {
                echo "❌ Build or publish failed!"
            }
        }
    }
}
