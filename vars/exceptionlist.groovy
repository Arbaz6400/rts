def SKIP_SCAN = "false"   // declare globally

def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/').replaceAll(/\.git$/, '')

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
