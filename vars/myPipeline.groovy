def call() {
    pipeline {
        agent any

        stages {
            stage('Checkout') {
                steps {
                    // Checkout your project repo (not the shared lib)
                    git branch: env.BRANCH_NAME, 
                        url: 'https://github.com/Arbaz6400/rts.git'
                }
            }

            stage('Set Version') {
                steps {
                    script {
                        def branch = env.BRANCH_NAME ?: "dev"
                        def baseVersion = "1.0.0"

                        if (branch.startsWith("release/")) {
                            baseVersion = branch.split("/")[1]
                        } else if (branch == "main") {
                            baseVersion = "1.0.0"
                        } else if (branch == "develop") {
                            baseVersion = "1.1.0"
                        }

                        def finalVersion
                        if (branch == "develop") {
                            finalVersion = "${baseVersion}-SNAPSHOT"
                        } else if (branch.startsWith("release/")) {
                            finalVersion = "${baseVersion}-RC"
                        } else if (branch == "main") {
                            finalVersion = baseVersion
                        } else {
                            finalVersion = "${baseVersion}-SNAPSHOT"
                        }

                        echo "📦 Using version: ${finalVersion}"

                        bat """
                          echo sed -i "s/^version = .*/version = \\"${finalVersion}\\"/" build.gradle
                        """
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "🛠️ Simulating Gradle build..."
                        bat 'echo gradlew.bat clean build'
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "📤 Simulating publish to Nexus..."
                        bat 'echo gradlew.bat publish'
                    }
                }
            }

            stage('Show Version') {
                steps {
                    script {
                        echo "✅ Simulating artifact version check..."
                        bat 'echo grep ^version build.gradle | cut -d "\\""\\" -f2'
                    }
                }
            }
        }
    }
}
