pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Arbaz6400/Streaming.git'
            }
        }

        stage('Load and Run Scripts') {
            steps {
                script {
                    def scripts = ['build.groovy', 'test.groovy', 'deploy.groovy']
                    for (s in scripts) {
                        def scriptPath = "${env.WORKSPACE}/${s}"
                        if (fileExists(scriptPath)) {
                            echo "Loading ${s}"
                            load scriptPath
                        } else {
                            error "Missing script: ${s}"
                        }
                    }
                }
            }
        }
    }
}
