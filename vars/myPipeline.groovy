def call() {
    pipeline {
        agent any

      //  environment {
          //  NEXUS_USER = credentials('nexus-username')
           // NEXUS_PASS = credentials('nexus-password')
   //     }

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Read build.gradle from Streaming repo
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")

                        // Match version = '1.0.0' or version = "1.0.0"
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        // Branch-based suffix logic
                        newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            // main/stg → no suffix, use base version
                            newVersion = baseVersion
                        }

                        echo "📌 Using version: ${newVersion}"
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "🛠️ Building with version ${newVersion}"
                       // sh "./gradlew clean build -Pversion=${newVersion}"
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "🚀 Publishing ${newVersion} to Nexus..." 
                    }
                }
            }
        }
    }
}
