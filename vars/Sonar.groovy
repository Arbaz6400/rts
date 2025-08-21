def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Setup Environment') {
                steps {
                    script {
                        env.SONARQUBE_URL = config.sonarUrl ?: "http://localhost:9000"
                        env.REPO_NAME     = config.repoName ?: env.JOB_NAME
                        env.CRED_ID       = config.credentialId ?: "sonar-token-id"
                    }
                }
            }

            stage('SonarQube Scan') {
                steps {
                    withCredentials([string(credentialsId: env.CRED_ID, variable: 'SONAR_TOKEN')]) {
                        sh """
                            ./gradlew sonarqube \
                                -Dsonar.projectKey=${env.REPO_NAME} \
                                -Dsonar.host.url=${env.SONARQUBE_URL} \
                                -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    script {
                        def status = sh(
                            script: """
                                curl -s -u ${SONAR_TOKEN}: ${env.SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${env.REPO_NAME} \
                                | jq -r '.projectStatus.status'
                            """,
                            returnStdout: true
                        ).trim()

                        if (status != "OK") {
                            error "❌ Quality Gate failed: ${status}"
                        } else {
                            echo "✅ Quality Gate passed!"
                        }
                    }
                }
            }
        }
    }
}
