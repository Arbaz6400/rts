// vars/exceptionPipeline.groovy
def call(String pipelineName, String identifier, String repoUrl, String branch, String filePath) {

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
                        def entry = "${pipelineName}:${identifier}"
                        if (exceptions.contains(entry)) {
                            echo "Skipping scan: ${entry} is in exception list."
                            currentBuild.description = "Skipped scan for ${entry}"
                            env.SKIP_SCAN = "true"
                        } else {
                            echo "${entry} not in exception list. Proceeding with scan."
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
                    echo "Running scan for ${pipelineName} / ${identifier}..."
                    // your scan command here
                }
            }
        }
    }
}
