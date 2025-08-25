def call() {
    pipeline {
        agent any
        environment {
            PROJECT_VERSION = ''
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
                        // Read build.gradle
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.+?)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "1.0.0" // default if not found

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Decide version: develop branch uses -SNAPSHOT
                        env.PROJECT_VERSION = env.BRANCH_NAME == 'develop' ? "${baseVersion}-SNAPSHOT" : baseVersion
                        echo "📌 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build & Publish') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_CREDS_USR', passwordVariable: 'NEXUS_CREDS_PSW')]) {
                        script {
                            // Ensure the Gradle tool is installed
                            def gradleHome
                            try {
                                gradleHome = tool name: 'Gradle-8.3', type: 'Gradle'
                            } catch(Exception e) {
                                error "Gradle-8.3 tool not found. Please install it in Jenkins global tools configuration."
                            }

                            echo "⚡ Running Gradle build and publish"

                            // Build
                            bat "${gradleHome}/bin/gradle.bat clean build -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW}"

                            // Publish
                            bat "${gradleHome}/bin/gradle.bat publish -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW} -PrepoUrl=https://nexus.yourcompany.com/repository/maven-snapshots/"
                        }
                    }
                }
            }
        }

        post {
            success { echo "✅ Build and publish completed successfully!" }
            failure { echo "❌ Build or publish failed!" }
        }
    }
}
