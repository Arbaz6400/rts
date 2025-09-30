def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

                        dir('exceptions') {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: '*/main']],
                                userRemoteConfigs: [[
                                    url: 'https://github.com/Arbaz6400/exception-list.git',
                                    credentialsId: 'git-creds-id'
                                ]]
                            ])
                        }

                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        echo "→ Exceptions loaded: ${exceptionsYaml}"
                        
                        def repoName = env.JOB_NAME.split('/')[1]   // adjust if needed
                        echo "→ Current repo: ${repoName}"

                        if (exceptionsYaml.exceptions.contains(repoName)) {
                            echo "→ Repo '${repoName}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${repoName}' is NOT in exceptions → running scan"
                            runScan(repoName)
                        }

                        echo "→ Exception list check finished"
                    }
                }
            }
        }
    }
}

def runScan(repo) {
    echo "Running scan for repo: ${repo}"
    sh "echo 'Scan logic goes here for ${repo}'"
}
