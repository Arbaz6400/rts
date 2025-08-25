def call() {
    pipeline {
        agent any

        environment {
            // keep your nexus credentials configured in Jenkins
            NEXUS_CREDS = credentials('nexus-creds')
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
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"
                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based version suffix
                        if (env.BRANCH_NAME == "develop") {
                            env.APP_VERSION = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            env.APP_VERSION = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            env.APP_VERSION = baseVersion
                        }

                        echo "📌 Using version: ${env.APP_VERSION}"
                    }
                }
            }

            stage('Check Nexus Credentials') {
                steps {
                    script {
                        echo "✅ Nexus credentials loaded"
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "⚡ Running Gradle build"
                        def gradleHome = tool name: 'Gradle-8.3', type: 'gradle'
                        bat "\"${gradleHome}/bin/gradle.bat\" clean build -Pversion=${env.APP_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW}"
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "⚡ Publishing to Nexus"
                        def gradleHome = tool name: 'Gradle-8.3', type: 'gradle'

                        // Automatically select snapshots vs releases repo
                        def repoUrl = env.APP_VERSION.endsWith('SNAPSHOT') ?
                            "https://nexus.yourcompany.com/repository/maven-snapshots/" :
                            "https://nexus.yourcompany.com/repository/maven-releases/"

                        bat "\"${gradleHome}/bin/gradle.bat\" publish -Pversion=${env.APP_VERSION} -PNEXUS_USERNAME=${NEXUS_CREDS_USR} -PNEXUS_PASSWORD=${NEXUS_CREDS_PSW} -PrepoUrl=${repoUrl}"
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
