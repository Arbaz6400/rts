def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

                        // Checkout exception list
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

                        // Read YAML
                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        echo "→ Exceptions loaded: ${exceptionsYaml}"

                        // Get current repo org/name from Git URL
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')
                        echo "→ Current org: ${orgRepo.split('/')[0]}, repo: ${orgRepo.split('/')[1]}"

                        // Check exceptions
                        if (exceptionsYaml.exceptions.contains(orgRepo)) {
                            echo "→ Repo '${orgRepo}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${orgRepo}' is NOT in exceptions → running scan"
                            runScan(orgRepo)
                        }

                        echo "→ Exception list check finished"
                    }
                }
            }
        }
    }
}

def runScan(orgRepo) {
    echo "Running scan for repo: ${orgRepo}"
    if (isUnix()) {
        sh "echo Scan logic goes here for ${orgRepo}"
    } else {
        bat "echo Scan logic goes here for ${orgRepo}"
    }
}
