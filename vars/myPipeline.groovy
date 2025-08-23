def call() {
    pipeline {
        agent any

        stages {
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

                        sh """
                          sed -i 's/^version = .*/version = "${finalVersion}"/' build.gradle
                        """
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "🛠️ Simulating Gradle build..."
                        sh "echo './gradlew clean build'"
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "📤 Simulating publish to Nexus..."
                        sh "echo './gradlew publish'"
                    }
                }
            }

            stage('Show Version') {
                steps {
                    script {
                        // Get version from build.gradle properly
                        def version = sh(
                            script: "grep '^version' build.gradle | cut -d '\"' -f2",
                            returnStdout: true
                        ).trim()

                        echo "   ✅ Artifact version: ${version}"
                    }
                }
            }
        }
    }
}
