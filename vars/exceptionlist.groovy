def call() {
    pipeline {
        agent any
        environment {
            SKIP_SCAN = 'false'
            RUN_SCAN  = 'false'
        }
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
                            env.SKIP_SCAN = 'true'
                            env.RUN_SCAN  = 'false'
                            echo "→ Repo '${orgRepo}' is in exceptions → skipping scan"
                        } else {
                            env.SKIP_SCAN = 'false'
                            env.RUN_SCAN  = 'true'
                            echo "→ Repo '${orgRepo}' is NOT in exceptions → running scan"
                            runScan(orgRepo)
                        }

                        // Print environment flags
                        echo "→ SKIP_SCAN = ${env.SKIP_SCAN}"
                        echo "→ RUN_SCAN  = ${env.RUN_SCAN}"

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
