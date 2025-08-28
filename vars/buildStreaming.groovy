def call() {
    pipeline {
        agent any

        environment {
            NEXUS_CREDENTIALS = credentials('nexus-creds')
        }

        stages {
            stage('Checkout Repos') {
                steps {
                    script {
                        // Checkout Streaming inside RTS workspace
                        dir('Streaming') {
                            checkout scm
                        }
                    }
                }
            }

            stage('Calculate Version') {
                steps {
                    script {
                        // Read version from Streaming/build.gradle
                        def buildFile = "Streaming/build.gradle"
                        def text = readFile(buildFile)
                        
                        // Extract version using regex
                        def matcher = text =~ /version\s*=\s*['"](.+)['"]/
                        def appVersion = matcher ? matcher[0][1] : "1.0.0"
                        
                        echo "Calculated APP_VERSION: ${appVersion}"
                        
                        // Store in env variable for next stages
                        env.APP_VERSION = appVersion
                    }
                }
            }

            stage('Update Version in build.gradle') {
                steps {
                    script {
                        def buildFile = "Streaming/build.gradle"
                        def text = readFile(buildFile)
                        text = text.replaceAll(/version\s*=\s*['"].*['"]/, "version = '${env.APP_VERSION}'")
                        writeFile(file: buildFile, text: text)

                        echo "Updated Streaming/build.gradle with version ${env.APP_VERSION}"
                    }
                }
            }
        }

        post {
            always {
                echo "Pipeline finished. APP_VERSION was ${env.APP_VERSION}"
            }
        }
    }
}
