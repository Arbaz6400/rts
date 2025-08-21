import com.mycompany.quality.QualityGate

def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            SONARQUBE_URL = config.sonarUrl ?: "http://localhost:9000"
            REPO_NAME     = config.repoName ?: env.JOB_NAME
            BRANCH_NAME   = config.branchName ?: env.BRANCH_NAME
        }

        stages {
            stage('SonarQube Scan') {
                steps {
                    withCredentials([string(credentialsId: config.credentialId ?: 'sonar-token-id', variable: 'SONAR_TOKEN')]) {
                        sh """
                            sonar-scanner \
                              -Dsonar.projectKey=${REPO_NAME} \
                              -Dsonar.projectName=${REPO_NAME} \
                              -Dsonar.sources=. \
                              -Dsonar.host.url=${SONARQUBE_URL} \
                              -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    script {
                        withCredentials([string(credentialsId: config.credentialId ?: 'sonar-token-id', variable: 'SONAR_TOKEN')]) {
                            def gate = new QualityGate(this)
                            // new signature: token, repoName, branchName
                            gate.check(SONAR_TOKEN, REPO_NAME, BRANCH_NAME)
                        }
                    }
                }
            }
        }
    }
}
