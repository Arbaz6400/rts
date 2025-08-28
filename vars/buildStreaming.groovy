def call(Map params = [:]) {
    pipeline {
        agent any

        environment {
            GRADLE_HOME = "/home/nonroot/.gradle7"
            NEXUS_USERNAME = credentials('rts-nexus-username')
            NEXUS_PASSWORD = credentials('rts-nexus-password')
        }

        stages {
            stage('Calculate Version') {
                steps {
                    script {
                        // Dynamic version based on branch
                        def branchName = env.BRANCH_NAME ?: 'main'
                        def baseVersion = "1.0.0"
                        env.APP_VERSION = branchName == 'master' ? baseVersion : "${baseVersion}-${branchName}"
                        echo "Calculated APP_VERSION: ${env.APP_VERSION}"
                    }
                }
            }

            stage('Build & Publish') {
                steps {
                    script {
                        // Call the buildStreaming function from Streaming repo
                        buildStreaming(
                            gradleArgs: "clean build install dependencies",
                            gradleTasks: "shadowJar",
                            gradleHome: env.GRADLE_HOME,
                            nexusUsername: env.NEXUS_USERNAME,
                            nexusPassword: env.NEXUS_PASSWORD,
                            appVersion: env.APP_VERSION
                        )
                    }
                }
            }
        }

        post {
            always {
                echo "Pipeline finished. APP_VERSION was ${env.APP_VERSION}"
            }
        }
    }
}
