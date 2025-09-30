def call() {
    pipeline {
        agent any
        environment {
            SKIP_SCAN = 'false'
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
                        // Extract org/repo from current SCM
                        def repoUrl = scm.userRemoteConfigs[0].url
                        if (repoUrl.endsWith(".git")) {
                            repoUrl = repoUrl[0..-5]
                        }
                        def parts = repoUrl.tokenize('/')
                        def orgRepo = parts[-2] + '/' + parts[-1]
                        echo "Repository detected from GitHub → ${orgRepo}"

                        // store in env so later stages can use
                        env.ORG_REPO = orgRepo

                        // read YAML exceptions
                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }
                        echo "Exceptions from YAML → ${exceptions}"

                        // determine if scan should be skipped
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

            stage('Debug') {
                steps {
                    script {
                        echo "DEBUG → SKIP_SCAN = ${env.SKIP_SCAN}"
                        echo "DEBUG → ORG_REPO = ${env.ORG_REPO}"
                    }
                }
            }

            stage('Run Scan') {
                when {
                    expression { env.SKIP_SCAN == 'false' }
                }
                steps {
                    echo "Running scan for repo → ${env.ORG_REPO}"
                    // Place your scanner logic here
                }
            }

            stage('Post-Success Task') {
                when {
                    expression { env.SKIP_SCAN == 'true' }
                }
                steps {
                    echo "Skipping scan for repo → ${env.ORG_REPO}, running post-success steps."
                    // Place any post-skip logic here
                }
            }
        }
    }
}
