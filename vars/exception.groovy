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
                    if (repoUrl.endsWith(".git")) repoUrl = repoUrl[0..-5]
                    def parts = repoUrl.tokenize('/')
                    def orgRepo = parts[-2] + '/' + parts[-1]

                    def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                    def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }

                    echo "Repository → ${orgRepo}"
                    echo "Exceptions → ${exceptions}"

                    skipScan = exceptions.contains(orgRepo)
                    echo "skipScan = ${skipScan}"
                }
            }
        }

        stage('Debug') {
            steps {
                script {
                    echo "DEBUG → skipScan = ${skipScan}"
                }
            }
        }

        stage('Run Scan') {
            when {
                expression { return skipScan == false }
            }
            steps {
                echo "Running scan"
            }
        }

        stage('Post-Success Task') {
            when {
                expression { return skipScan == true }
            }
            steps {
                echo "Skipping scan"
            }
        }
    }
}
