def run() {
    pipeline {
        agent any
        stages {
            stage('Build Stage') {
                steps {
                    echo "Running Build pipeline from RTS repo"
                }
            }
        }
    }
}
return this
