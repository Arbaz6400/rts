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
            echo "🌿 Detected branch: ${branch}"

            def baseVersion = "1.0.0"

            if (branch.startsWith("release/")) {
                baseVersion = branch.split("/")[1]   // take part after release/
                finalVersion = "${baseVersion}-RC"
            } else if (branch == "main") {
                baseVersion = "1.0.0"
                finalVersion = baseVersion
            } else if (branch == "develop") {
                baseVersion = "1.1.0"
                finalVersion = "${baseVersion}-SNAPSHOT"
            } else {
                finalVersion = "${baseVersion}-SNAPSHOT"
            }

            echo "📦 Using version: ${finalVersion}"

            bat "echo sed -i \"s/^version = .*/version = '${finalVersion}'/\" build.gradle"
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
                        echo "✅ Artifact version: ${finalVersion}"
                    }
                }
            }
        }
    }
}
