def call() {
    pipeline {
        agent any
        environment {
            // Environment variables if needed
            NEXUS_USERNAME = credentials('NEXUS_CREDS_USR')
            NEXUS_PASSWORD = credentials('NEXUS_CREDS_PSW')
        }
        stages {

            stage('Checkout SCM') {
                steps {
                    echo "🔄 Checking out source code..."
                    checkout scm
                }
            }

            stage('Versioning') {
                steps {
                    script {
                        // Read build.gradle
                        def gradleFile = readFile('build.gradle')
                        echo "📖 Base version from build.gradle: ${gradleFile}"

                        // Extract version using regex
                        def matcher = gradleFile =~ /version\s*=\s*['"](.+?)['"]/
                        env.PROJECT_VERSION = matcher ? matcher[0][1] : '1.0.0'

                        echo "📌 Using version: ${env.PROJECT_VERSION}"
                    }
                }
            }

            stage('Build & Publish') {
                steps {
                    script {
                        echo "⚡ Running Gradle build and publish..."

                        // Use Gradle tool
                        def gradleHome = tool name: 'Gradle-8.3', type: 'Gradle'

                        // Build
                        bat "${gradleHome}\\bin\\gradle.bat clean build -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${env.NEXUS_USERNAME} -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}"

                        // Publish
                        bat "${gradleHome}\\bin\\gradle.bat publish -Pversion=${env.PROJECT_VERSION} -PNEXUS_USERNAME=${env.NEXUS_USERNAME} -PNEXUS_PASSWORD=${env.NEXUS_PASSWORD}"
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Build & publish succeeded!"
            }
            failure {
                echo "❌ Build or publish failed!"
            }
        }
    }
}
