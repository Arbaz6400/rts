def call(Map config = [:]) {
    // Optional config
    def APP_VERSION = config.get('appVersion', '1.0.0')

    pipeline {
        agent any
        environment {
            APP_VERSION = "${APP_VERSION}"
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    script {
                        if (isUnix()) {
                            sh "./gradlew clean shadowJar -PappVersion=${APP_VERSION}"
                            def jarFile = sh(script: "ls build/libs/*-all.jar", returnStdout: true).trim()
                            echo "Generated JAR: ${jarFile}"
                        } else {
                            bat "gradlew.bat clean shadowJar -PappVersion=${APP_VERSION}"
                            def jarFile = bat(script: "dir /b build\\libs\\*-all.jar", returnStdout: true).trim()
                            echo "Generated JAR: ${jarFile}"
                        }
                    }
                }
            }

            stage('Upload') {
                when {
                    expression { false } // Skip actual upload
                }
                steps {
                    echo "Skipping upload"
                }
            }
        }

        post {
            always {
                echo "Build finished for version: ${APP_VERSION}"
            }
        }
    }
}
