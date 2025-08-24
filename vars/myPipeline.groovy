def call(Map config = [:]) {
    pipeline {
        agent any
        stages {
            stage('Versioning') {
                steps {
                    script {
                        def branch = env.BRANCH_NAME
                        def version
                        if (branch == 'main') {
                            version = "1.0.${env.BUILD_NUMBER}-SNAPSHOT"
                        } else if (branch.startsWith("release")) {
                            version = "1.0.${env.BUILD_NUMBER}-RC"
                        } else {
                            version = "1.0.${env.BUILD_NUMBER}"
                        }

                        echo "📌 Using version: ${version}"
                    }
                }
            }
        }
    }
}
