pipeline {
    agent any

    environment {
        ORG_REPO = ''  // will be set dynamically
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
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            url: 'https://github.com/Arbaz6400/exception-list.git'
                        ]]
                    ])
                }
            }
        }

        stage('Check Exception List') {
            steps {
                script {
                    ORG_REPO = "${env.JOB_NAME}" // or dynamically detect repo
                    def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                    def exceptions = exceptionsYaml.exceptions

                    boolean SKIP_SCAN = exceptions.contains(ORG_REPO)
                    echo "Repository → ${ORG_REPO}"
                    echo "Exceptions → ${exceptions}"
                    echo "DEBUG → SKIP_SCAN = ${SKIP_SCAN}"

                    // Store it in environment so it can be accessed in other stages
                    env.SKIP_SCAN = SKIP_SCAN.toString()
                }
            }
        }

        stage('Run Scan') {
            when { 
                expression { return env.SKIP_SCAN.toBoolean() == false } 
            }
            steps {
                echo "Running scan → ${ORG_REPO}"
                // Your actual scan commands here
            }
        }

        stage('Post-Success Task') {
            when { 
                expression { return env.SKIP_SCAN.toBoolean() == false } 
            }
            steps {
                echo "Post-scan tasks for ${ORG_REPO}"
            }
        }
    }
}
