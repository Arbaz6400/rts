def call() {

    pipeline {
        agent any

        stages {
            stage('Generate Cluster Jobs') {
                steps {
                    jobDsl(
                        targets: 'dsl/clusters.groovy',
                        additionalParameters: [
                            WORKSPACE_PATH: env.WORKSPACE
                        ],
                        sandbox: false
                    )
                }
            }
        }
    }
}
