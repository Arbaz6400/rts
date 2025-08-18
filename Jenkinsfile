echo ">>> DEBUG: Loaded Jenkinsfile version 2"
pipeline {
    agent any

    parameters {
        string(name: 'TARGET_REPO', defaultValue: 'Leap1', description: 'Which Leap repo triggered this build')
    }

    stages {
        stage('Debug') {
            steps {
                echo "Triggered by repo: ${params.TARGET_REPO}"
            }
        }

        stage('Load Pipeline') {
            steps {
                // Load the right groovy file based on TARGET_REPO
                script {
                    def pipelineFile = "${params.TARGET_REPO.toLowerCase()}.groovy"
                    echo "Loading pipeline: ${pipelineFile}"
                    load pipelineFile
                }
            }
        }
    }
}
