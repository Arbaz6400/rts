def call() {
    pipeline {
        agent any

        environment {
            // Make sure these are defined in Jenkins credentials or environment
            NEXUS_USERNAME = credentials('nexus-username-id')
            NEXUS_PASSWORD = credentials('nexus-password-id')
        }

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Read build.gradle from Streaming repo
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")

                        // Match version = '1.0.0' or version = "1.0.0"
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            // main/stg → no suffix, use base version
                            newVersion = baseVersion
                        }

                        env.PROJECT_VERSION = newVersion
                        echo "📌 Using project version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "⚡ Building project with version ${env.PROJECT_VERSION}"
                        // Use Gradle wrapper if available, else Jenkins Gradle tool
                        def gradleCmd = fileExists('gradlew.bat') ? 'gradlew.bat' : "${tool 'Gradle-8.3'}/bin/gradle.bat"
                        bat "${gradleCmd} clean build -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${env.NEXUS_USERNAME} -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}"
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "🚀 Publishing project version ${env.PROJECT_VERSION}"

                        // Determine repository URL based on version
                        def repoUrl = env.PROJECT_VERSION.endsWith("SNAPSHOT") ?
                                      "https://nexus.yourcompany.com/repository/maven-snapshots/" :
                                      "https://nexus.yourcompany.com/repository/maven-releases/"

                        def gradleCmd = fileExists('gradlew.bat') ? 'gradlew.bat' : "${tool 'Gradle-8.3'}/bin/gradle.bat"

                        bat "${gradleCmd} publish -Pversion=${env.PROJECT_VERSION} " +
                            "-PNEXUS_USERNAME=${env.NEXUS_USERNAME} -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD} " +
                            "-PrepositoryUrl=${repoUrl}"
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build and publish successful: ${env.PROJECT_VERSION}"
            }
            failure {
                echo "❌ Build or publish failed!"
            }
        }
    }
}
