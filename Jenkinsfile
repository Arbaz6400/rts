pipeline {
    agent any

    environment {
        SONAR_TOKEN   = credentials('sonar-token-id')      // Replace with your credential ID
        SONARQUBE_URL = "http://localhost:9000"            // Your Sonar URL
        PROJECT_KEY   = "your-project-key"                 // Replace with your project key
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
                    // instantiate and call class from src/
                    def gate = new com.mycompany.QualityGate(this)
                    gate.check(PROJECT_KEY, SONAR_TOKEN, SONARQUBE_URL)
                }
            }
        }
    }
}
