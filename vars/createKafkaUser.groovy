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
            env.GENERATED_PASSWORD = bat(
                script: '''python -c "import secrets, base64; print(base64.b64encode(secrets.token_bytes(16)).decode())"''',
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
                        bat """
                            set BOOTSTRAP=${env.BOOTSTRAP}
                            set NEW_USER=${env.NEW_USER}
                            set PASSWORD=${env.GENERATED_PASSWORD}
                            set ADMIN_USER=${env.ADMIN_USER}
                            set ADMIN_PASS=${env.ADMIN_PASS}

                            python create_user.py
                        """
                    }
                }
            }
        }
    }
}
