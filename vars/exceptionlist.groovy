def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

                        // Checkout the exceptions repo
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

                        // Read the exceptions YAML
                        def exceptionsYaml = readYaml file: 'exceptions/exceptions.yaml'
                        echo "→ Exceptions loaded: ${exceptionsYaml}"

                        // Determine current repo
                        def repoName = env.JOB_NAME.split('/')[1]   // adjust if needed
                        echo "→ Current repo: ${repoName}"

                        // Check if repo is in exception list
                        if (exceptionsYaml.contains(repoName)) {
                            echo "→ Repo '${repoName}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${repoName}' is NOT in exceptions → running scan"
                            runScan(repoName) // your existing scan logic
                        }

                        echo "→ Exception list check finished"
                    }
                }
            }
        }
    }
}

// Example helper function
def runScan(repo) {
    echo "Running scan for repo: ${repo}"
    sh "echo 'Scan logic goes here for ${repo}'"
}
