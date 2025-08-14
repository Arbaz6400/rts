def run() {
    pipeline {
        agent any
        stages {
            stage('Deploy Stage') {
                steps {
                    echo "Running Deploy pipeline from RTS repo"
                }
            }
        }
    }
}
return this
