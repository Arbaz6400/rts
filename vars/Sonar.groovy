import com.mycompany.quality.QualityGate

def call(String repoName, String branchName) {
    pipeline {
        agent any

        environment {
            SONAR_TOKEN   = credentials('sonar-token-id')
            SONARQUBE_URL = "http://localhost:9000"
        }

        stages {
            stage('SonarQube Scan') {
                steps {
                    sh """
                        sonar-scanner \
                          -Dsonar.projectKey=${repoName} \
                          -Dsonar.branch.name=${branchName} \
                          -Dsonar.host.url=${SONARQUBE_URL} \
                          -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }

            stage('Quality Gate') {
                steps {
                    script {
                        def gate = new QualityGate(this)
                        gate.check(SONAR_TOKEN, repoName, branchName)
                    }
                }
            }
        }
    }
}
