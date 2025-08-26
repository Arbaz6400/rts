def call(Map config = [:]) {

    pipeline {
        agent any

        environment {
            NEXUS = credentials('nexus-creds')
            GRADLE_HOME = "C:/gradle" // adjust as needed
        }

        stages {
            stage('Perform Gradle Release') {
                when {
                    not { triggeredBy 'TimerTrigger' }
                    expression { return env.BRANCH_NAME == 'master' }
                }
                steps {
                    script {
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'nexus-creds', 
                                usernameVariable: 'NEXUS_USERNAME', 
                                passwordVariable: 'NEXUS_PASSWORD'
                            )
                        ]) {
                            gradleWrapper.release(
                                'clean', 
                                '-Prelease tag', 
                                env.NEXUS_USERNAME, 
                                env.NEXUS_PASSWORD, 
                                GRADLE_HOME
                            )
                        }
                    }
                }
            }

            stage('Perform Gradle Build') {
                steps {
                    script {
                        shadowJar = true
                        echo "Gradle tasks: clean build publish"
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'nexus-creds', 
                                usernameVariable: 'NEXUS_USERNAME', 
                                passwordVariable: 'NEXUS_PASSWORD'
                            )
                        ]) {
                            gradleWrapper.printGitState()
                            gradleBuild(
                                gradleWrapper, 
                                gradle_args, 
                                gradle_tasks += 'printVersion', 
                                env.NEXUS_USERNAME, 
                                env.NEXUS_PASSWORD, 
                                GRADLE_HOME
                            )
                        }
                    }
                }
            }

            stage('Push to Nexus') {
                steps {
                    script {
                        def nexus = new org.enbd.common.NexusRest(this)

                        // Read POM
                        def pomLocation = 'build/publications/mavenJava/pom-default.xml'
                        def pom = readMavenPom file: pomLocation

                        // Compute branch-based version
                        def branch = env.BRANCH_NAME
                        def version = pom.version
                        if (branch != 'master') {
                            version = "${version}-SNAPSHOT"
                        }

                        // Determine JAR path
                        def jar_location = shadowJar ?
                            "build/libs/${pom.artifactId}-${version}-all.jar" :
                            "build/libs/${pom.artifactId}-${version}.jar"

                        // Determine Nexus repo
                        def isMaster = (branch == 'master')
                        def isSnapshot = version.contains('SNAPSHOT')
                        def repoType = isSnapshot ? 'snapshot' : 'release'
                        def nexusRepo = "${config.nexusRepository}-${repoType}"
                        def engNexusRepo = "${config.engNexusRepository}-${repoType}"

                        // Upload
                        if (isMaster && !isSnapshot) {
                            echo "Uploading to Prod Nexus: ${jar_location}"
                            nexus.uploadReleaseProdNexus(pomLocation, nexusRepo, shadowJar)
                        } else {
                            echo "Uploading to Engineering Nexus: ${jar_location}"
                            nexus.uploadEngNexusArtifact(
                                pomLocation, 
                                engNexusRepo, 
                                shadowJar, 
                                "${env.NEXUS_USERNAME}:${env.NEXUS_PASSWORD}", 
                                false
                            )
                        }
                    }
                }
            }

            stage('Push Production Release Tag') {
                when {
                    branch 'master'
                    not { triggeredBy 'TimerTrigger' }
                    expression { return !config.skipTagging } 
                }
                steps {
                    script {
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'GITBDMPRODUSR', 
                                usernameVariable: 'GITHUB_USERNAME', 
                                passwordVariable: 'GITHUB_PASSWORD'
                            )
                        ]) {
                            gradleWrapper.pushTags(
                                config.git_repo, 
                                "${GITHUB_USERNAME}", 
                                "${GITHUB_PASSWORD}"
                            )
                        }
                    }
                }
            }
        }
    }
}
