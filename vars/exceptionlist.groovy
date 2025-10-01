

def call() {
    // Local flags inside the pipeline
    def SKIP_SONAR = "false"
    def SKIP_CHECKMARX = "false"

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

                        echo "→ Sonar exceptions: ${exceptionsYaml.exceptions.sonar}"
                        echo "→ Checkmarx exceptions: ${exceptionsYaml.exceptions.checkmarx}"

                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')

                        if (exceptionsYaml.exceptions.sonar?.contains(orgRepo)) {
                            SKIP_SONAR = "true"
                        }
                        if (exceptionsYaml.exceptions.checkmarx?.contains(orgRepo)) {
                            SKIP_CHECKMARX = "true"
                        }

                        echo "→ SKIP_SONAR=${SKIP_SONAR}, SKIP_CHECKMARX=${SKIP_CHECKMARX}"

                        // store them into env so they can be used in later stages
                        env.SKIP_SONAR = SKIP_SONAR
                        env.SKIP_CHECKMARX = SKIP_CHECKMARX
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
