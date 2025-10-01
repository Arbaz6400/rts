def call() {
    pipeline {
        agent any
        environment {
            SKIP_SCAN = "false"
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

                        // Get current repo org/name
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')
                        echo "→ Current org: ${orgRepo.split('/')[0]}, repo: ${orgRepo.split('/')[1]}"

                        // Check exceptions
                        if (exceptionsYaml.exceptions.contains(orgRepo)) {
                            env.SKIP_SCAN = "true"
                            echo "→ Repo '${orgRepo}' is in exceptions → skipping scan"
                        } else {
                            env.SKIP_SCAN = "false"
                            echo "→ Repo '${orgRepo}' is NOT in exceptions → running scan"
                        }

                        echo "→ Set SKIP_SCAN = ${env.SKIP_SCAN}"
                    }
                }
            }

            stage('Scan Status') {
                steps {
                    script {
                        if (env.SKIP_SCAN == "true") {
                            echo "→ Scan skipped ❌ (SKIP_SCAN=${env.SKIP_SCAN})"
                        } else {
                            echo "→ Scan executed 🚀 (SKIP_SCAN=${env.SKIP_SCAN})"
                        }
                    }
                }
            }
        }
    }
}
