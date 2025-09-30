// vars/exceptionlist.groovy
import hudson.FilePath
import groovy.yaml.YamlSlurper

def call() {
    node {
        stage('Exception List Check') {
            echo "→ Starting exception list check"

            // Directory to clone the exception list repo
            def exceptionDir = 'exceptions'

            // Clone the exceptions repo
            dir(exceptionDir) {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Arbaz6400/exception-list.git',
                        credentialsId: 'git-creds-id' // update with your Jenkins credential
                    ]]
                ])
            }

            // Read YAML exceptions
            def exceptionsYaml = readYaml file: "${exceptionDir}/exceptions.yaml"
            echo "→ Exceptions loaded: ${exceptionsYaml}"

            // Determine current repo name
            def currentRepo = env.JOB_NAME.tokenize('/').last()
            echo "→ Current repo: ${currentRepo}"

            // Check if current repo is in exceptions
            if (exceptionsYaml.exceptions.contains(currentRepo)) {
                echo "→ Repo '${currentRepo}' is in exceptions → skipping scan"
            } else {
                echo "→ Repo '${currentRepo}' is NOT in exceptions → running scan"
                runScan(currentRepo)
            }
        }
    }
}

def runScan(repo) {
    echo "→ Running scan for repo: ${repo}"

    if (isUnix()) {
        sh "echo Scan logic goes here for ${repo}"  // replace with actual scan command
    } else {
        bat "echo Scan logic goes here for ${repo}" // Windows compatible
    }
}
