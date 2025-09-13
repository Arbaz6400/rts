def call() {
    pipeline {
        agent any
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

            stage('Decide Scan') {
                steps {
                    script {
                        // repoName derived from JOB_NAME → org/Leap2/main → "Leap2"
                        def repoName = env.JOB_NAME?.tokenize('/')?.getAt(1)
                        echo "Repository detected: ${repoName}"

                        // load exception list
                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString() }

                        if (exceptions.contains(repoName)) {
                            echo "${repoName} is in exception list → Skipping scan."
                            currentBuild.result = 'SUCCESS'
                        } else {
                            echo "${repoName} not in exception list → Running scan."
                            // trigger scan logic here
                        }
                    }
                }
            }
        }
    }
}
