def call(String pipelineName) {
    pipeline {
        agent any
        stages {
            stage('Checkout Exception List') {
                steps {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/exception-list.git']]
                    ])
                }
            }

            stage('Decide Scan') {
                steps {
                    script {
                        // Get repo name from JOB_NAME, e.g. org/Leap2/main → Leap2
                        def repoName = env.JOB_NAME?.tokenize('/')?.getAt(1)
                        echo "Pipeline: ${pipelineName}, Identifier: ${repoName}"

                        // Load exception list
                        def yamlData = readYaml file: 'exceptions.yaml'
                        def exceptions = (yamlData?.pipelines?.get(pipelineName) ?: []).collect { it.toString() }

                        if (exceptions.contains(repoName)) {
                            echo "${repoName} is in exception list → Skipping scan."
                            currentBuild.result = 'SUCCESS'
                        } else {
                            echo "${repoName} not in exception list → Running scan."
                            // your scan stage goes here
                        }
                    }
                }
            }
        }
    }
}
