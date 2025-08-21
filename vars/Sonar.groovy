import com.mycompany.quality.QualityGate

def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            SONARQUBE_URL = "http://localhost:9000"  // keep static here
        }

        stages {
            stage('SonarQube Scan') {
                steps {
                    script {
                        // compute dynamic values here
                        def repoName   = config.repoName   ?: env.JOB_NAME
                        def branchName = config.branchName ?: "main"
                        def sonarUrl   = config.sonarUrl   ?: env.SONARQUBE_URL
                        def credId     = config.credentialId ?: 'sonar-token-id'

                        withCredentials([string(credentialsId: credId, variable: 'SONAR_TOKEN')]) {
                            sh """
                                sonar-scanner \
                                  -Dsonar.projectKey=${repoName} \
                                  -Dsonar.projectName=${repoName} \
                                  -Dsonar.sources=. \
                                  -Dsonar.host.url=${sonarUrl} \
                                  -Dsonar.login=${SONAR_TOKEN}
                            """
                        }

                        // Save to env for next stage
                        env.REPO_NAME   = repoName
                        env.BRANCH_NAME = branchName
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    script {
                        withCredentials([string(credentialsId: config.credentialId ?: 'sonar-token-id', variable: 'SONAR_TOKEN')]) {
                            def gate = new QualityGate(this)
                            gate.check(SONAR_TOKEN, env.REPO_NAME, env.BRANCH_NAME)
                        }
                    }
                }
            }
        }
    }
}
