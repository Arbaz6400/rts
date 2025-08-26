def call(Map config = [:]) {
    // Define gradleWrapper here
    def gradleWrapper = new GradleWrapper(steps: this)  // assuming GradleWrapper class accepts steps

    // Environment variables
    def git_repo = config.git_repo ?: "https://github.com/Arbaz6400/Streaming.git"
    def nexusRepository = config.nexusRepository ?: "maven-releases"
    def engNexusRepository = config.engNexusRepository ?: "maven-snapshots"
    def skipTagging = config.skipTagging ?: false
    def gradle_home = config.gradle_home ?: "C:/gradle"

    pipeline {
        agent any

        environment {
            NEXUS = credentials('nexus-creds')
            GRADLE_HOME = gradle_home
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
                                gradle_home
                            )
                        }
                    }
                }
            }

            stage('Perform Gradle Build') {
                steps {
                    script {
                        def shadowJar = true
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
                                gradle_home
                            )
                        }
                    }
                }
            }

            stage('Push to Nexus') {
                when {
                    not { triggeredBy 'TimerTrigger' }
                }
                steps {
                    script {
                        def branch = env.BRANCH_NAME
                        def isMaster = (branch == 'master')
                        def pomLocation = 'build/publications/mavenJava/pom-default.xml'
                        def pom = readMavenPom file: pomLocation
                        def isSnapshot = pom.version.contains('SNAPSHOT')
                        def nexusRepo = isSnapshot ? engNexusRepository : nexusRepository

                        if (isMaster && !isSnapshot) {
                            echo "Uploading to Prod Nexus..."
                            nexusRest.uploadReleaseProdNexus(pomLocation, nexusRepository, shadowJar)
                        } else {
                            echo "Uploading to Engineering Nexus..."
                            nexusRest.uploadEngNexusArtifact(
                                pomLocation, 
                                nexusRepo, 
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
                    expression { return !skipTagging } 
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
                                git_repo, 
                                "${env.GITHUB_USERNAME}", 
                                "${env.GITHUB_PASSWORD}"
                            )
                        }
                    }
                }
            }
        }
    }
}
