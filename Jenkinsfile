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
                    // Read Groovy class file content
                    def classText = readFile 'src/com/mycompany/QualityGate.groovy'
                    // Load class via evaluate()
                    def clazz = evaluate(classText)
                    // Create instance of the class
                    def gate = clazz.newInstance(this)
                    // Call the check method
                    gate.check(PROJECT_KEY, SONAR_TOKEN, SONARQUBE_URL)
                }
            }
        }
    }
}
