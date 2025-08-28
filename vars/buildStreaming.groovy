def call(Map params = [:]) {
    node {
        stage('Build Streaming') {
            echo "Gradle Tasks: ${params.gradleTasks}"
            echo "Using Gradle Home: ${params.gradleHome}"
            echo "App Version: ${params.appVersion}"

            withCredentials([usernamePassword(
                credentialsId: 'nexus-creds', 
                usernameVariable: 'NEXUS_USERNAME', 
                passwordVariable: 'NEXUS_PASSWORD')]) {

                // Example gradle command
                bat "${params.gradleHome}\\bin\\gradle ${params.gradleTasks} -PnexusUser=${NEXUS_USERNAME} -PnexusPass=${NEXUS_PASSWORD} -PappVersion=${params.appVersion}"
            }
        }
    }
}
