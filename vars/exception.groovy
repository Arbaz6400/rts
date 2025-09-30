// vars/exception.groovy
def call() {
    pipeline {
        agent any

        environment {
            // default, if you still need env var
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
                        def repoUrl = scm.userRemoteConfigs[0].url
                        if (repoUrl.endsWith(".git")) repoUrl = repoUrl[0..-5]
                        def parts = repoUrl.tokenize('/')
                        def orgRepo = parts[-2] + '/' + parts[-1]

                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }

                        echo "Repository → ${orgRepo}"
                        echo "Exceptions → ${exceptions}"

                        // use a groovy variable for reliable conditional
                        env.SKIP_SCAN = exceptions.contains(orgRepo).toString()
                        env.ORG_REPO = orgRepo
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
                    echo "Running scan → ${env.ORG_REPO}"
                }
            }

            stage('Post-Success Task') {
                when {
                    expression { env.SKIP_SCAN == 'true' }
                }
                steps {
                    echo "Skipping scan → ${env.ORG_REPO}"
                }
            }
        }
    }
}
