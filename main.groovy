properties([
    parameters([
        choice(name: 'PIPELINE_TYPE', choices: ['build', 'test', 'deploy'], description: 'Select which pipeline to run')
    ])
])

pipeline {
    agent any
    stages {
        stage("Load Pipeline") {
            steps {
                script {
                    echo "Running pipeline: ${PIPELINE_TYPE}"
                    load "pipelines/${PIPELINE_TYPE}.groovy"
                }
            }
        }
    }
}
