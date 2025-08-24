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

                        // PowerShell to replace version in build.gradle
                        bat """
                        powershell -Command "(Get-Content build.gradle) -replace '^version = .*', 'version = \\"${finalVersion}\\"' | Set-Content build.gradle"
                        """
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "🛠️ Simulating Gradle build..."
                        bat "gradlew.bat clean build"
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "📤 Simulating publish to Nexus..."
                        bat "gradlew.bat publish"
                    }
                }
            }

            stage('Show Version') {
                steps {
                    script {
                        def version = bat(
                            script: 'powershell -Command "(Select-String \\"^version\\" build.gradle).Line.Split(\\"\\")[1]"',
                            returnStdout: true
                        ).trim()

                        echo "   ✅ Artifact version: ${version}"
                    }
                }
            }
        }
    }
}
