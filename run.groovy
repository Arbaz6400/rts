@Library('my-shared-lib') _
import utils.MyFunctions

pipeline {
    agent any
    stages {
        stage('Test Import') {
            steps {
                script {
                    def msg = MyFunctions.customMessage("Arbaz")
                    echo msg
                }
            }
        }
    }
}
