def call(Map params = [:]) {
    pipeline {
    agent any
    stages {
        stage('Prepare Version') {
            steps {
                script {
                    // Example: dynamic version based on branch
                    def branchName = env.BRANCH_NAME ?: 'main'
                    env.APP_VERSION = branchName == 'main' ? "1.0.0" : "1.0.0-${branchName}"
                    echo "Calculated APP_VERSION: ${env.APP_VERSION}"
                }
            }
        }
        stage('Build Streaming') {
            steps {
                script {
                    // Use the correct Jenkins credential ID
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus-creds', 
                        usernameVariable: 'NEXUS_USERNAME', 
                        passwordVariable: 'NEXUS_PASSWORD')]) {
                        
                        buildStreaming(
                            gradleArgs: "clean build install dependencies",
                            gradleTasks: "shadowJar",
                            gradleHome: env.GRADLE_HOME,
                            nexusUsername: NEXUS_USERNAME,
                            nexusPassword: NEXUS_PASSWORD,
                            appVersion: env.APP_VERSION
                        )
                    }
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
