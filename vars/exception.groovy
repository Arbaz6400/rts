pipeline {
    agent any

    environment {
        SKIP_SCAN = false   // default
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Checkout Exception List') {
            steps {
                dir('exceptions') {
                    git url: 'https://github.com/Arbaz6400/exception-list.git', branch: 'main'
                }
            }
        }

        stage('Check Exception List') {
            steps {
                script {
                    // Read YAML file
                    def exceptions = readYaml file: 'exceptions/exceptions.yaml'
                    
                    // Detect repo
                    def repo = env.GIT_URL.tokenize('/').takeRight(2).join('/')
                    echo "Repository detected from GitHub → ${repo}"
                    
                    if (exceptions.contains(repo)) {
                        echo "${repo} is in exception list → will skip scan."
                        env.SKIP_SCAN = 'true'
                    } else {
                        echo "${repo} not in exception list → will run scan."
                        env.SKIP_SCAN = 'false'
                    }
                }
            }
        }

        stage('Run Scan') {
            when {
                expression { return env.SKIP_SCAN == 'false' }
            }
            steps {
                echo "Running scan for repo → ${env.GIT_URL}"
                // Your sonar scan steps here
            }
        }

        stage('Post-Success Task') {
            when {
                expression { return env.SKIP_SCAN == 'false' }
            }
            steps {
                echo "Post-scan tasks running..."
            }
        }
    }
}
