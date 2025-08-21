def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            SONARQUBE_URL = config.sonarUrl ?: "http://localhost:9000"
            REPO_NAME     = config.repoName ?: env.JOB_NAME
            SONAR_CRED_ID = config.credentialId ?: "sonar-token-id"
        }

        stages {
            stage('SonarQube Scan') {
                steps {
                    withCredentials([string(credentialsId: SONAR_CRED_ID, variable: 'SONAR_AUTH')]) {
                        sh """
                            sonar-scanner \
                              -Dsonar.projectKey=${REPO_NAME} \
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
                        gate.check(SONAR_CRED_ID, REPO_NAME)
                    }
                }
            }
        }
    }
}
