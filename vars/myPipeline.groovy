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
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.+?)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "1.0.0"

                        echo "📖 Base version from build.gradle: ${baseVersion}"
                        env.PROJECT_VERSION = env.BRANCH_NAME == 'develop' ? "${baseVersion}-SNAPSHOT" : baseVersion
                        echo "📌 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build & Publish') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_CREDS_USR', passwordVariable: 'NEXUS_CREDS_PSW')]) {
                        script {
                            echo "⚡ Running Gradle build and publish"
                            def gradleHome = tool name: 'Gradle-8.3', type: 'Gradle'

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
