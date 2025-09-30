def SKIP_SCAN = false  // top-level variable

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
                    def ORG_REPO = parts[-2] + '/' + parts[-1]
                    echo "Repository detected from GitHub → ${ORG_REPO}"

                    def yamlData = readYaml file: 'exceptions/exceptions.yaml'
                    def exceptions = (yamlData?.exceptions ?: []).collect { it.toString().trim() }
                    echo "Exceptions from YAML → ${exceptions}"

                    if (exceptions.contains(ORG_REPO)) {
                        echo "${ORG_REPO} is in exception list → will skip scan."
                        SKIP_SCAN = true
                    } else {
                        echo "${ORG_REPO} not in exception list → will run scan."
                        SKIP_SCAN = false
                    }

                    // store repo for later use
                    env.ORG_REPO = ORG_REPO
                }
            }
        }

        stage('Run Scan') {
            when {
                expression { return !SKIP_SCAN }  // skip if SKIP_SCAN = true
            }
            steps {
                echo "Running scan for repo → ${env.ORG_REPO}"
                // actual scan logic here
            }
        }

        stage('Post-Success Task') {
            when {
                expression { return SKIP_SCAN }  // only runs if skipped
            }
            steps {
                echo "Skipping scan for repo → ${env.ORG_REPO}, running post-success steps."
            }
        }
    }
}
