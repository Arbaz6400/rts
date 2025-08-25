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
                        // Parse version, fallback to 1.0.0 if not found
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "1.0.0"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Decide SNAPSHOT suffix for develop branch
                        env.PROJECT_VERSION = env.BRANCH_NAME == 'develop' ? "${baseVersion}-SNAPSHOT" : baseVersion
                        echo "📌 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Check Nexus Credentials') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_CREDS_USR', passwordVariable: 'NEXUS_CREDS_PSW')]) {
                        echo "✅ Nexus credentials loaded"
                    }
                }
            }

            stage('Build') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_CREDS_USR', passwordVariable: 'NEXUS_CREDS_PSW')]) {
                        script {
                            echo "⚡ Running Gradle build"
                            // Make sure this Gradle installation exists in Jenkins: Manage Jenkins → Global Tool Configuration → Gradle
                            def gradleHome = tool name: 'Gradle-8.3', type: 'Gradle'
                            bat "${gradleHome}/bin/gradle.bat clean build -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW}"
                        }
                    }
                }
            }

            stage('Publish') {
                steps {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_CREDS_USR', passwordVariable: 'NEXUS_CREDS_PSW')]) {
                        script {
                            echo "⚡ Publishing to Nexus"
                            def gradleHome = tool name: 'Gradle-8.3', type: 'Gradle'
                            bat "${gradleHome}/bin/gradle.bat publish -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW} -PrepoUrl=https://nexus.yourcompany.com/repository/maven-snapshots/"
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
                echo "❌ Build or publish failed!"
            }
        }
    }
}
