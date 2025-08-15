pipeline {
    agent any
    stages {
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
