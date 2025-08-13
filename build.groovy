pipeline {
    agent any

    stages {
        stage('Checkout Streaming Repo') {
            steps {
                // Clean the workspace first (optional)
                cleanWs()

                // Clone the 'streaming' repository
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']], // Change to the branch you want
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[$class: 'CleanBeforeCheckout']], // Optional
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Arbaz6400/streaming.git' // Change to your repo URL
                    ]]
                ])
            }
        }
    }
}
