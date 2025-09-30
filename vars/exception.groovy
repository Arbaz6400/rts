def skipScan = false

pipeline {
    agent any
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
                    if (repoUrl.endsWith(".git")) {
                        repoUrl = repoUrl[0..-5]
                    }
                    def parts = repoUrl.tokenize('/')
                    def orgRepo = parts[-2] + '/' + parts[-1]
                    echo "Repository detected from GitHub → ${orgRepo}"

                    def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                    def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }
                    echo "Exceptions from YAML → ${exceptions}"

                    if (exceptions.contains(orgRepo)) {
                        echo "${orgRepo} is in exception list → will skip scan."
                        skipScan = true
                    } else {
                        echo "${orgRepo} not in exception list → will run scan."
                        skipScan = false
                    }

                    env.ORG_REPO = orgRepo
                }
            }
        }

        stage('Debug') {
            steps {
                script {
                    echo "DEBUG → skipScan = ${skipScan}"
                    echo "DEBUG → ORG_REPO = ${env.ORG_REPO}"
                }
            }
        }

        stage('Run Scan') {
            when {
                expression { !skipScan }
            }
            steps {
                echo "Running scan for repo → ${env.ORG_REPO}"
                // scanner logic here
            }
        }

        stage('Post-Success Task') {
            when {
                expression { skipScan }
            }
            steps {
                echo "Skipping scan for repo → ${env.ORG_REPO}, running post-success steps."
            }
        }
    }
}
