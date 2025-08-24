def call() {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Read version from Streaming repo build.gradle
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*"(.*)"/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Append suffix depending on branch
                        def branch = env.BRANCH_NAME
                        def finalVersion = baseVersion

                        if (branch == "develop") {
                            finalVersion = "${baseVersion}-SNAPSHOT"
                        } else if (branch == "release") {
                            finalVersion = "${baseVersion}-RC"
                        } else if (branch in ["main", "stg"]) {
                            // No suffix
                            finalVersion = baseVersion
                        } else {
                            echo "⚠️ Unknown branch '${branch}', keeping base version"
                        }

                        echo "📌 Using version: ${finalVersion}"

                        // Export for later stages (like Nexus publish)
                        env.PROJECT_VERSION = finalVersion
                    }
                }
            }

            // Example build stage (disabled for now)
            // stage('Build') {
            //     steps {
            //         sh "./gradlew build -Pversion=${env.PROJECT_VERSION}"
            //     }
            // }
        }
    }
}
