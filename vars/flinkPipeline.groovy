def call(Map args) {

    pipeline {
        agent any

        stages {
            stage('Test Job DSL') {
                steps {
                    echo "Running pipeline for cluster: ${args.cluster}"
                }
            }
        }
    }
}
