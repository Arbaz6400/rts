// vars/exceptionHandler.groovy
def call(String identifier, String repoUrl, String branch, String filePath, Closure scanStage) {
    pipeline {
        agent any
        stages {
            stage('Checkout Exception List') {
                steps {
                    dir('exceptions') {
                        git branch: branch, url: repoUrl
                    }
                }
            }

            stage('Check Exception List') {
                steps {
                    script {
                        def exceptions = readFile("exceptions/${filePath}").split("\n")*.trim()
                        if (exceptions.contains(identifier)) {
                            echo "Skipping scan: ${identifier} is in exception list."
                            currentBuild.description = "Skipped scan for ${identifier}"
                            env.SKIP_SCAN = "true"
                        } else {
                            echo "${identifier} not in exception list. Proceeding with scan."
                            env.SKIP_SCAN = "false"
                        }
                    }
                }
            }

            stage('Run Scan') {
                when {
                    expression { return env.SKIP_SCAN == "false" }
                }
                steps {
                    script {
                        scanStage()
                    }
                }
            }
        }
    }
}
