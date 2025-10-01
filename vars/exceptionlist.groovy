

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

                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'

                        // Init defaults
                        def SKIP_SONAR = "false"
                        def SKIP_CHECKMARX = "false"

                        // Global skip check
                        if (exceptionsYaml.exceptions.global?.'sonar-skip' == true) {
                            SKIP_SONAR = "true"
                            echo "🌍 Global Sonar skip enabled → all Sonar scans skipped"
                        }
                        if (exceptionsYaml.exceptions.global?.'checkmarx-skip' == true) {
                            SKIP_CHECKMARX = "true"
                            echo "🌍 Global Checkmarx skip enabled → all Checkmarx scans skipped"
                        }

                        // Only check repo-level exceptions if not globally skipped
                        if (SKIP_SONAR == "false") {
                            def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                            def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')
                            if (exceptionsYaml.exceptions.sonar?.contains(orgRepo)) {
                                SKIP_SONAR = "true"
                                echo "→ Repo '${orgRepo}' is in Sonar exceptions → skipping Sonar"
                            }
                        }

                        if (SKIP_CHECKMARX == "false") {
                            def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                            def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')
                            if (exceptionsYaml.exceptions.checkmarx?.contains(orgRepo)) {
                                SKIP_CHECKMARX = "true"
                                echo "→ Repo '${orgRepo}' is in Checkmarx exceptions → skipping Checkmarx"
                            }
                        }

                        // Export to env for later stages
                        env.SKIP_SONAR = SKIP_SONAR
                        env.SKIP_CHECKMARX = SKIP_CHECKMARX

                        echo "→ Final flags: SKIP_SONAR=${env.SKIP_SONAR}, SKIP_CHECKMARX=${env.SKIP_CHECKMARX}"
                    }
                }
            }

            stage('Sonar Scan') {
                when { expression { env.SKIP_SONAR == "false" } }
                steps {
                    echo "🚀 Running SonarQube Scan"
                }
            }

            stage('Checkmarx Scan') {
                when { expression { env.SKIP_CHECKMARX == "false" } }
                steps {
                    echo "🔍 Running Checkmarx Scan"
                }
            }
        }
    }
}

