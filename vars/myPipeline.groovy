def call() {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
                        def matcher = gradleFile =~ /version\s*=\s*['"](.*)['"]/
                        def baseVersion = matcher ? matcher[0][1] : "0.0.1"

                        echo "📖 Base version from build.gradle: ${baseVersion}"

                        newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME in ["main", "stg"]) {
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
                        def gradle = new org.enbd.common.GradleWrapper(this)
                        withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                                                         usernameVariable: 'NEXUS_USER',
                                                         passwordVariable: 'NEXUS_PASS')]) {
                            gradle.build(
                                "-Pversion=${newVersion}",
                                "clean build",
                                env.NEXUS_USER,
                                env.NEXUS_PASS,
                                "${env.WORKSPACE}/.gradle"
                            )
                        }
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "🚀 Publishing ${newVersion} to Nexus..."
                        def nexus = new org.enbd.common.NexusRest(this)
                        def pomPath = "build/publications/mavenJava/pom-default.xml"
                        nexus.uploadReleaseProdNexus(pomPath, "maven-releases", false)
                    }
                }
            }
        }
    }
}
