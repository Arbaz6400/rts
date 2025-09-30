def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

                        // Checkout the exception list
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

                        // Load exceptions.yaml
                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        echo "→ Exceptions loaded: ${exceptionsYaml}"

                        // Extract org and repo from the Git URL
                        def gitUrl = env.GIT_URL ?: 'https://github.com/Leap-stream/Leap2.git'
                        def parts = gitUrl.tokenize('/')     // simple split by '/'
                        def orgName = parts[-2]
                        def repoName = parts[-1].replaceAll(/\.git$/, '')

                        echo "→ Current org: ${orgName}, repo: ${repoName}"

                        // Check against exceptions list
                        def fullRepo = "${orgName}/${repoName}"
                        if (exceptionsYaml.exceptions.contains(fullRepo)) {
                            echo "→ Repo '${fullRepo}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${fullRepo}' is NOT in exceptions → running scan"
                            runScan(repoName)
                        }

                        echo "→ Exception list check finished"
                    }
                }
            }
        }
    }
}

// Scan logic with cross-platform support
def runScan(repo) {
    echo "Running scan for repo: ${repo}"
    if (isUnix()) {
        sh "echo Scan logic goes here for ${repo}"
    } else {
        bat "echo Scan logic goes here for ${repo}"
    }
}
