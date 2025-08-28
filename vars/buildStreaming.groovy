def call() {
    pipeline {
    agent any

    environment {
        // Compute the version based on branch
        APP_VERSION = getVersionForBranch(env.BRANCH_NAME)
    }

    stages {
        stage('Build') {
            steps {
                script {
                    echo "Building with version: ${env.APP_VERSION}"
                    // Pass version to Gradle build
                    sh "./gradlew clean shadowJar -PappVersion=${env.APP_VERSION}"
                }
            }
        }

        stage('Upload') {
            steps {
                script {
                    // Upload the -all.jar file
                    def jarName = "streaming-${env.APP_VERSION}-all.jar"
                    echo "Uploading ${jarName}..."
                    sh "curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD -T build/libs/${jarName} https://nexus.example.com/repository/releases/"
                }
            }
        }
    }


// Example function to map branch to version
def getVersionForBranch(String branch) {
    if (branch == 'master') {
        return "1.0.0"
    } else if (branch.startsWith('release/')) {
        return branch.replace('release/', '') + "-RC"
    } else {
        return "1.0.0-${branch}"
    }
}


        post {
            always {
                echo "Pipeline finished. APP_VERSION was ${env.APP_VERSION}"
            }
        }
    }
}
