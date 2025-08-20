pipeline {
    agent any

    environment {
        SONAR_TOKEN   = credentials('sonar-token-id')
        SONARQUBE_URL = "http://localhost:9000"
        PROJECT_KEY   = "your-project-key"
    }

    stages {
        stage('SonarQube Scan') {
            steps {
                sh """
                    sonar-scanner -Dsonar.projectKey=${PROJECT_KEY} \
                                   -Dsonar.host.url=${SONARQUBE_URL} \
                                   -Dsonar.login=${SONAR_TOKEN}
                """
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    // LOAD the QualityGate class file
                    def qualityGateScript = load "src/com/mycompany/QualityGate.groovy"
                    // Call the check method from loaded script
                    qualityGateScript.check(PROJECT_KEY, SONAR_TOKEN, SONARQUBE_URL)
                }
            }
        }
    }
}
