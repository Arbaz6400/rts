def run() {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    echo "Running build from RTS repo"
                }
            }
        }
    }
}
return this
