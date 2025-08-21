import com.mycompany.quality.QualityGate

def call(String repoName) {
    pipeline {
    agent any

    environment {
        SONARQUBE_URL = "http://localhost:9000"
        PROJECT_KEY   = "your-project-key"
    }

    stages {
        stage('SonarQube Scan') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token-id', variable: 'SONAR_AUTH')]) {
                    sh """
                        sonar-scanner -Dsonar.projectKey=${PROJECT_KEY} \
                                      -Dsonar.host.url=${SONARQUBE_URL} \
                                      -Dsonar.login=${SONAR_AUTH}
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    def gate = new com.mycompany.quality.QualityGate(this)
                    gate.check("sonar-token-id", PROJECT_KEY, SONARQUBE_URL)
                }
            }
        }
    }
}
