def call() {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    git branch: env.BRANCH_NAME,
                        url: 'https://github.com/Arbaz6400/rts.git'
                }
            }

            stage('Set Version') {
                steps {
                    script {
                        echo "🌿 Detected branch: ${env.BRANCH_NAME}"

                        // Read current version from build.gradle
                        def gradleFile = readFile('build.gradle')
                        def currentVersion = (gradleFile =~ /version\s*=\s*['"]([^'"]+)['"]/)[0][1]

                        // Logic for version suffix
                        if (env.BRANCH_NAME == "main") {
                            finalVersion = currentVersion.replace("-SNAPSHOT", "").replace("-RC", "")
                        } else if (env.BRANCH_NAME == "release") {
                            finalVersion = currentVersion.replace("-SNAPSHOT", "") + "-RC"
                        } else {
                            finalVersion = currentVersion.replace("-RC", "") + "-SNAPSHOT"
                        }

                        echo "📦 Using version: ${finalVersion}"

                        // Simulate modifying build.gradle (just echo, no actual sed)
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
                        bat 'echo gradlew clean build'
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "📤 Simulating publish to Nexus..."
                        bat 'echo gradlew publish'
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
}
