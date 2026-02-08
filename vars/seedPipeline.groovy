def call() {
    pipeline {
        agent any
        stages {
            stage('Generate Jobs') {
                steps {
                    jobDsl(
                        targets: 'dsl/clusters.groovy',
                        removedJobAction: 'DELETE'
                    )
                }
            }
        }
    }
}
