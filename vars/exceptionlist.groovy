def call() {
    pipeline {
        agent any
        stages {
            stage('Exception List Check') {
                steps {
                    script {
                        echo "→ Starting exception list check"

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
                        echo "→ Exceptions loaded: ${exceptionsYaml}"
                        
                        def gitUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        def orgRepo = gitUrl.split('/')[-2..-1].join('/')  // Leap-stream/Leap2
                        def repo = gitUrl.split('/')[-1].replace('.git', '') // Leap2
                        echo "→ Current org: ${orgRepo.split('/')[0]}, repo: ${repo}"

                        if (exceptionsYaml.exceptions.contains(orgRepo)) {
                            echo "→ Repo '${orgRepo}' is in exceptions → skipping scan"
                        } else {
                            echo "→ Repo '${orgRepo}' is NOT in exceptions → running scan"
                            runScan(orgRepo)   // pass full org/repo
                        }

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
