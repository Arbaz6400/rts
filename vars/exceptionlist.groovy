// Global variable
def SKIP_SCAN = "false"

def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

                        // Checkout exception list repo
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

                        // Print entire list in logs
                        echo "→ Full exception list: ${exceptionsYaml.exceptions}"

                        // Get current repo org/name
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')
                        echo "→ Current org: ${orgRepo.split('/')[0]}, repo: ${orgRepo.split('/')[1]}"

                        // Check if current repo is in exception list
                        if (exceptionsYaml.exceptions.contains(orgRepo)) {
                            SKIP_SCAN = "true"
                            echo "→ Repo '${orgRepo}' is in exceptions → skipping scan"
                        } else {
                            SKIP_SCAN = "false"
                            echo "→ Repo '${orgRepo}' is NOT in exceptions → running scan"
                        }

                        echo "→ SKIP_SCAN now set to: ${SKIP_SCAN}"
                    }
                }
            }

            stage('Scan Status') {
                steps {
                    script {
                        if (SKIP_SCAN == "true") {
                            echo "→ Scan skipped ❌ (SKIP_SCAN=${SKIP_SCAN})"
                        } else {
                            echo "→ Scan executed 🚀 (SKIP_SCAN=${SKIP_SCAN})"
                        }
                    }
                }
            }
        }
    }
}
