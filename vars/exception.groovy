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
                        def repoUrl = scm.userRemoteConfigs[0].url
                        if (repoUrl.endsWith(".git")) repoUrl = repoUrl[0..-5]
                        def parts = repoUrl.tokenize('/')
                        def orgRepo = parts[-2] + '/' + parts[-1]
                        env.ORG_REPO = orgRepo

                        def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                        def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }

                        echo "Repository detected → ${orgRepo}"
                        echo "Exceptions → ${exceptions}"

                        // use a Groovy boolean variable
                        boolean skip = exceptions.contains(orgRepo)
                        env.SKIP_SCAN = skip.toString()  // store string for other stages if needed
                        echo "SKIP_SCAN boolean = ${skip}"
                    }
                }
            }

            stage('Debug') {
                steps {
                    script {
                        echo "DEBUG → SKIP_SCAN string = ${env.SKIP_SCAN}"
                        echo "DEBUG → ORG_REPO = ${env.ORG_REPO}"
                    }
                }
            }

            stage('Run Scan') {
                when {
                    expression { 
                        return env.SKIP_SCAN.toBoolean() == false
                    }
                }
                steps {
                    echo "Running scan for repo → ${env.ORG_REPO}"
                }
            }

            stage('Post-Success Task') {
                when {
                    expression { 
                        return env.SKIP_SCAN.toBoolean() == true
                    }
                }
                steps {
                    echo "Skipping scan for repo → ${env.ORG_REPO}"
                }
            }
        }
    }
}
