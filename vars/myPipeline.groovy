def call() {
    pipeline {
        agent any

        environment {
            // Nexus credentials from Jenkins (username/password)
            NEXUS = credentials('nexus-creds')
            // Ensure Gradle is on PATH (Chocolatey installation path)
            PATH = "C:\\ProgramData\\chocolatey\\bin;${env.PATH}"
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
                            newVersion = baseVersion
                        }

                        env.APP_VERSION = newVersion
                        echo "📌 Using version: ${env.APP_VERSION}"
                    }
                }
            }

            stage('Build & Publish') {
                steps {
                    script {
                        echo "⚡ Running Gradle build and publish"

                        if (isUnix()) {
                            sh "./gradlew clean build publish -PnexusUsername=${env.NEXUS_USR} -PnexusPassword=${env.NEXUS_PSW} -Pversion=${env.APP_VERSION}"
                        } else {
                            bat "gradle clean build publish -PnexusUsername=%NEXUS_USR% -PnexusPassword=%NEXUS_PSW% -Pversion=%APP_VERSION%"
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
