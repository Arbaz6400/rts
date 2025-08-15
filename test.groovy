pipeline {
    agent any
    stages {
        stage('Check for Tag') {
            steps {
                script {
                    if (env.TAG_NAME && env.TAG_NAME.trim()) {
                        error "❌ This job cannot run on a tag build! Tag detected: ${env.TAG_NAME}"
                    } else {
                        echo "✅ No tag detected. Proceeding..."
                    }
                }
            }
        }

        stage('Git Config Setup') {
            steps {
                script {
                    sh """
                        git config --global user.name "Arbaz6400"
                        git config --global user.email "Shaikharbaz525@gmail.com"
                    """
                    echo "✅ Git username and email configured."
                }
            }
        }

        stage('Test Stage') {
            steps {
                echo "✅ Test Groovy from RTS repo executed successfully!"
                echo "Running on node: ${env.NODE_NAME}"
                echo "Workspace: ${env.WORKSPACE}"
            }
        }
    }
    post {
        always {
            echo "✅ Finished test pipeline."
        }
    }
}
