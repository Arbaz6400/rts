// vars/buildStreaming.groovy
def call(String branchName, String gradleHome, String nexusUsername, String nexusPassword) {
    pipeline {
        agent any

        environment {
            NEXUS_USERNAME = nexusUsername
            NEXUS_PASSWORD = nexusPassword
        }

        stages {
            stage('Determine Version') {
                steps {
                    script {
                        // Branch-based versioning
                        def APP_VERSION
                        if (branchName == 'master') {
                            APP_VERSION = "1.1.0"
                        } else if (branchName.startsWith('release')) {
                            APP_VERSION = "1.1.0-RC"
                        } else {
                            APP_VERSION = "1.1.0-SNAPSHOT"
                        }
                        echo "Dynamic APP_VERSION = ${APP_VERSION}"
                        env.APP_VERSION = APP_VERSION
                    }
                }
            }

            stage('Build Streaming Repo') {
                steps {
                    script {
                        def gradle = new org.enbd.common.GradleWrapper(steps)
                        gradle.build(
                            gradleArgs: "clean build shadowJar",
                            gradleTasks: "",
                            username: env.NEXUS_USERNAME,
                            password: env.NEXUS_PASSWORD,
                            gradleHome: gradleHome,
                            appVersion: env.APP_VERSION
                        )
                    }
                }
            }
        }
    }
}
