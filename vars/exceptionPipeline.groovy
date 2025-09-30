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
                        // Full path: org/repo/branch
                        def parts = env.JOB_NAME?.tokenize('/')
                        def orgName  = parts?.getAt(0)
                        def repoName = parts?.getAt(1)

                        def fullName = "${orgName}/${repoName}"
                        echo "Repository detected: ${fullName}"

                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString() }

                        if (exceptions.contains(fullName)) {
                            echo "${fullName} is in exception list → will skip scan."
                            env.SKIP_SCAN = 'true'
                        } else {
                            echo "${fullName} not in exception list → will run scan."
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
