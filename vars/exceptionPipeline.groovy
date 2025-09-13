def call() {
    pipeline {
        agent any
        environment {
            SKIP_SCAN = 'false'   // default
        }
        stages {
            stage('Checkout Exception List') {
                steps {
                    dir('exceptions') {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: '*/main']],
                            userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/exception-list.git']]
                        ])
                    }
                }
            }

            stage('Check Exception List') {
                steps {
                    script {
                        // repoName derived from JOB_NAME → org/Leap2/main → "Leap2"
                        def repoName = env.JOB_NAME?.tokenize('/')?.getAt(1)
                        echo "Repository detected: ${repoName}"

                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString() }

                        if (exceptions.contains(repoName)) {
                            echo "${repoName} is in exception list → will skip scan."
                            env.SKIP_SCAN = 'true'
                        } else {
                            echo "${repoName} not in exception list → will run scan."
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
                    echo "Running scan for repo → ${env.JOB_NAME}"
                    // your actual scan logic here
                }
            }

            stage('Post-Success Task') {
                when {
                    expression { return env.SKIP_SCAN == 'true' }
                }
                steps {
                    echo "Skipping scan but still doing other tasks..."
                }
            }
        }
    }
}
