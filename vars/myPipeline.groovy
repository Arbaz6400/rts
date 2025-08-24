pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/Arbaz6400/rts.git'
            }
        }

        stage('Set Version') {
            steps {
                script {
                    echo "🌿 Detected branch: ${env.BRANCH_NAME}"

                    def baseVersion = readFile('build.gradle')
                        .split('\n')
                        .find { it.trim().startsWith('version') }
                        ?.replaceAll(/version\s*=\s*['"](.+)['"]/, '$1')

                    if (!baseVersion) {
                        error "❌ Could not find version in build.gradle"
                    }

                    if (env.BRANCH_NAME.startsWith("release/")) {
                        finalVersion = baseVersion + "-RC"
                    } else {
                        finalVersion = baseVersion + "-SNAPSHOT"
                    }

                    echo "📦 Using version: ${finalVersion}"

                    // Simulate sed update (Windows safe, just echo)
                    bat """
                    echo sed -i "s/^version = .*/version = '${finalVersion}'/" build.gradle
                    """
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "🛠️ Simulating Gradle build..."
                    bat "echo gradlew clean build"
                }
            }
        }

        stage('Publish') {
            steps {
                script {
                    echo "📤 Simulating publish to Nexus..."
                    bat "echo gradlew publish"
                }
            }
        }

        stage('Show Version') {
            steps {
                script {
                    echo "✅ Artifact version: ${finalVersion}"
                }
            }
        }
    }
}
