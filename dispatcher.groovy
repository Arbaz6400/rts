pipeline {
    agent any
    parameters {
        choice(
            name: 'PIPELINE_NAME',
            choices: ['build.groovy', 'deploy.groovy'],
            description: 'Select which RTS pipeline to run'
        )
    }
    stages {
        stage('Run Selected Pipeline') {
            steps {
                script {
                    def scriptPath = "${env.WORKSPACE}/${params.PIPELINE_NAME}"
                    echo "Loading RTS pipeline: ${scriptPath}"
                    load(scriptPath)
                }
            }
        }
    }
}
