def call() {
    pipeline {
        agent any

        environment {
            NEXUS_USERNAME = credentials('nexus-username')
            NEXUS_PASSWORD = credentials('nexus-password')
        }

        stages {
            stage('Versioning') {
                steps {
                    script {
                        // Read base version from Streaming repo's build.gradle
                        def gradleFile = readFile("${env.WORKSPACE}/build.gradle")
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
                            newVersion = baseVersion
                        } else {
                            newVersion = "${baseVersion}-${env.BRANCH_NAME}"
                        }

                        echo "📌 Final version: ${newVersion}"

                        // Rewrite build.gradle so Gradle uses this version
                        gradleFile = gradleFile.replaceAll(/version\s*=\s*['"].*['"]/, "version = '${newVersion}'")
                        writeFile(file: "${env.WORKSPACE}/build.gradle", text: gradleFile)
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        def gradle = new org.enbd.common.GradleWrapper(this)
                        gradle.build("--stacktrace --no-daemon",
                                     "clean build publish",
                                     env.NEXUS_USERNAME,
                                     env.NEXUS_PASSWORD,
                                     "${env.WORKSPACE}/.gradle")
                    }
                }
            }

            stage('Publish') {
                steps {
                    script {
                        echo "🚀 Published version ${newVersion} to Nexus"
                    }
                }
            }
        }
    }
}
