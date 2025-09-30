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

                        // Get org/repo from SCM URL
                        def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def parts = scmUrl.split('/')
                        def orgName = parts[-2]          // second-to-last part
                        def repoName = parts[-1].replace('.git','')  // last part without .git
                        echo "→ Current org: ${orgName}, repo: ${repoName}"

                        // Check if repo is in exceptions
                        if (exceptionsYaml.exceptions.contains("${orgName}/${repoName}")) {
                            echo "→ Repo '${orgName}/${repoName}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${orgName}/${repoName}' is NOT in exceptions → running scan"
                            runScan(repoName)
                        }

                        echo "→ Exception list check finished"
                    }
                }
            }
        }
    }
}

// Scan logic
def runScan(repo) {
    echo "Running scan for repo: ${repo}"
    if (isUnix()) {
        sh "echo Scan logic goes here for ${repo}"
    } else {
        bat "echo Scan logic goes here for ${repo}"
    }
}
