pipeline {
    agent any
    stages {
        stage('Check for Tag') {
            steps {
                script {
                    // Explicit UTF-8 reading mode
                    if (env.TAG_NAME && env.TAG_NAME.trim()) {
                        error "❌ This job cannot run on a tag build! Tag detected: ${env.TAG_NAME}"
                    } else {
                        echo "✅ No tag detected. Proceeding..."
                    }
                }
            }
        }

        stage('Load Groovy from Git') {
            steps {
                script {
                    // Clone RTS repo or fetch updates
                    sh 'rm -rf rts'
                    sh 'git clone https://github.com/Arbaz6400/rts.git'

                    // Read Groovy file in UTF-8
                    def groovyContent = readFile(
                        file: 'rts/myScript.groovy',
                        encoding: 'UTF-8'
                    )

                    // Run the script inside pipeline
                    evaluate(groovyContent)
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
