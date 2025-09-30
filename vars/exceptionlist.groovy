pipeline {
    agent any
    environment {
        SKIP_SCAN = 'false'  // default
        ORG_REPO = ''        // will be set dynamically
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
                    // Detect repo name dynamically if needed
                    ORG_REPO = env.JOB_NAME.split('/')[-1] // optional, or set manually
                    def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                    def exceptions = exceptionsYaml.exceptions // must be a list
                    echo "Repository → ${ORG_REPO}"
                    echo "Exceptions → ${exceptions}"

                    // Determine if scan should be skipped
                    if (exceptions.contains(ORG_REPO)) {
                        env.SKIP_SCAN = 'true'
                        echo "${ORG_REPO} is in exception list → will skip scan."
                    } else {
                        env.SKIP_SCAN = 'false'
                        echo "${ORG_REPO} is not in exception list → will run scan."
                    }
                }
            }
        }

        stage('Debug') {
            steps {
                script {
                    echo "DEBUG → SKIP_SCAN = ${env.SKIP_SCAN}"
                    echo "DEBUG → ORG_REPO = ${ORG_REPO}"
                }
            }
        }

        stage('Run Scan') {
            when {
                expression { return env.SKIP_SCAN.toBoolean() == false }
            }
            steps {
                echo "Running scan → ${ORG_REPO}"
                // Add your actual scan command here, e.g.
                // sh './gradlew sonarScan'
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
