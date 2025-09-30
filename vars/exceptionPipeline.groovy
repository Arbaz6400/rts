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
                        // Build org/repo from the last 3 parts of JOB_NAME
                        def parts = env.JOB_NAME?.tokenize('/')
                        def orgRepo = parts[-3] + '/' + parts[-2]
                        echo "Repository detected: ${orgRepo}"

                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }

                        echo "Exceptions from YAML → ${exceptions}"

                        if (exceptions.contains(orgRepo)) {
                            echo "${orgRepo} is in exception list → will skip scan."
                            env.SKIP_SCAN = 'true'
                        } else {
                            echo "${orgRepo} not in exception list → will run scan."
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
