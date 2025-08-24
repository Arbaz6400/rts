pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: env.BRANCH_NAME, url: 'https://github.com/Arbaz6400/rts.git'
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

                    // Update build.gradle with version
                    if (isUnix()) {
                        sh """sed -i 's/^version = .*/version = "${finalVersion}"/' build.gradle"""
                    } else {
                        bat """powershell -Command "(Get-Content build.gradle) -replace '^version = .*', 'version = \\"${finalVersion}\\"' | Set-Content build.gradle" """
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    gradleHelper.run("clean build", true) // dry-run for now
                }
            }
        }

        stage('Publish') {
            steps {
                script {
                    gradleHelper.run("publish", true) // will later remove dry-run
                }
            }
        }

        stage('Show Version') {
            steps {
                script {
                    def version
                    if (isUnix()) {
                        version = sh(script: "grep '^version' build.gradle | cut -d '\"' -f2", returnStdout: true).trim()
                    } else {
                        version = bat(script: 'powershell -Command "(Get-Content build.gradle | Select-String \'^version\').ToString().Split(\'\"\')[1]"', returnStdout: true).trim()
                    }
                    echo "✅ Artifact version: ${version}"
                }
            }
        }
    }
}
