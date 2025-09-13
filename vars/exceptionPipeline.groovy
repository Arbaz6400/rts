// vars/exceptionPipeline.groovy
def call(String pipelineName, String identifier) {

    pipeline {
        agent any
        stages {
            stage('Checkout Exception List') {
                steps {
                    dir('exceptions') {
                        git branch: 'main', url: 'https://github.com/Arbaz6400/exception-list.git'
                    }
                }
            }

            stage('Check Exception List') {
                steps {
                    script {
                        def yamlData = readYaml file: "exceptions/exceptions.yaml"

                        def exceptions = yamlData?.pipelines?.get(pipelineName) ?: []
                        if (exceptions.contains(identifier)) {
                            echo "Skipping scan: ${pipelineName}:${identifier} is in exception list."
                            currentBuild.description = "Skipped scan for ${pipelineName}:${identifier}"
                            env.SKIP_SCAN = "true"
                        } else {
                            echo "${pipelineName}:${identifier} not in exception list. Proceeding with scan."
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
                    // replace with actual scan command
                }
            }
        }
    }
}
