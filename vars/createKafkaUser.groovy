def call() {

    pipeline {
        agent any

        environment {
            BOOTSTRAP = "localhost:9092"
            NEW_USER  = "app_user_${env.BUILD_NUMBER}"
        }

        stages {
            stage('Generate Password') {
                steps {
                    script {
                        env.GENERATED_PASSWORD = sh(
                            script: "openssl rand -base64 16",
                            returnStdout: true
                        ).trim()
                    }
                }
            }

            stage('Load Python Script') {
                steps {
                    script {
                        writeFile file: 'create_user.py',
                            text: libraryResource('kafka/create_user.py')
                    }
                }
            }

            stage('Create Kafka User') {
                steps {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'kafka-admin',
                            usernameVariable: 'ADMIN_USER',
                            passwordVariable: 'ADMIN_PASS'
                        )
                    ]) {
                        sh """
                            export PATH=\$HOME/.local/bin:\$PATH
                            export BOOTSTRAP=${env.BOOTSTRAP}
                            export NEW_USER=${env.NEW_USER}
                            export PASSWORD=${env.GENERATED_PASSWORD}

                            python3 create_user.py
                        """
                    }
                }
            }
        }
    }
}
