def call() {
    pipeline {
        agent any
        stages {
            stage('Demo Stage') {
                steps {
                    echo "Shared pipeline stage is running"
                }
            }
        }
    }
}
