def call() {
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

                        def baseVersion = "1.0.0"
                        def finalVersion

                        if (env.BRANCH_NAME == "develop") {
                            finalVersion = baseVersion + "-SNAPSHOT"
                        } else if (env.BRANCH_NAME.startsWith("release/")) {
                            finalVersion = baseVersion + "-RC"
                        } else if (env.BRANCH_NAME == "main") {
                            finalVersion = baseVersion
                        } else {
                            finalVersion = baseVersion + "-SNAPSHOT"
                        }

                        echo "📦 Using version: ${finalVersion}"

                        // Just echo the sed command (don’t execute for now)
                        bat """
                            echo sed -i "s/^version = .*/version = '${finalVersion}'/" build.gradle
                        """

                        env.ARTIFACT_VERSION = finalVersion
                    }
                }
            }

            stage('Build') {
                steps {
                    bat "echo gradlew clean build"
                }
            }

            stage('Publish') {
                steps {
                    bat "echo gradlew publish"
                }
            }

            stage('Show Version') {
                steps {
                    echo "✅ Artifact version: ${env.ARTIFACT_VERSION}"
                }
            }
        }
    }
}
