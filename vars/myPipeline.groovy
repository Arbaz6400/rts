import org.rts.utils.VersionUtils

def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        def versionUtils = new VersionUtils(this)
                        def baseVersion = versionUtils.getProjectVersion()

                        echo "📖 Base version: ${baseVersion}"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            newVersion = baseVersion
                        }

                        env.APP_VERSION = newVersion
                        echo "📌 Final version: ${env.APP_VERSION}"
                    }
                }
            }


        stage('Upload to Nexus') {
            steps {
                script {
                    // Define variables
            def pomLocation = 'build/publications/mavenJava/pom-default.xml'
            def nexusRepository = 'releases'
            def shadowJar = false // set to false because your JAR is not -all.jar

            // Initialize NexusRest from library
            def nexusRest = new org.enbd.common.NexusRest(this)

            // Upload the JAR and POM
            nexusRest.uploadReleaseProdNexus(pomLocation, nexusRepository, shadowJar
                                            )
                }
            }
        }
        }
    }
}
