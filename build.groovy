pipeline {
    agent any

    stages {
        stage('Checkout Remote Repo') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/your-org/streaming.git',
                        credentialsId: 'github-token'
                    ]]
                ])
            }
        }
    }
}
