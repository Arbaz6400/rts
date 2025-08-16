pipeline {
    agent any
    stages {
        stage('Check for Tag') {
            steps {
                script {
                    // Explicit UTF-8 reading mode
                    
                        echo "✅ No tag detected. deploy.groovy Proceeding..."
                    }
                }
            }
    }
    post {
        always {
            echo "✅ Finished test pipeline."
        }
    }
}
