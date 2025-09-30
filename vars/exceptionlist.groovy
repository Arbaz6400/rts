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

                        // Read exceptions YAML
                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        echo "→ Exceptions loaded: ${exceptionsYaml}"

                        // Extract org and repo from main repo URL
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def parts = gitUrl.tokenize('/')            // split URL by '/'
                        def org = parts[-2]                         // second last part is org
                        def repo = parts[-1].replace('.git','')     // last part is repo (remove .git)
                        def orgRepo = "${org}/${repo}"
                        echo "→ Current org: ${org}, repo: ${repo}"

                        // Check against exceptions
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

// Run scan logic
def runScan(orgRepo) {
    echo "Running scan for repo: ${orgRepo}"
    if (isUnix()) {
        sh "echo Scan logic goes here for ${orgRepo}"
    } else {
        bat "echo Scan logic goes here for ${orgRepo}"
    }
}
