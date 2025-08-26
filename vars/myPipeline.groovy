import org.enbd.common.GradleWrapper
import org.enbd.common.NexusRest

// vars/myPipeline.groovy
def call() {
    pipeline {
        agent any

        environment {
            NEXUS = credentials('nexus-creds')
            // Do not set GRADLE_HOME here; will be set dynamically in script
        }

        stages {
            stage('Setup Gradle Home') {
                steps {
                    script {
                        // Groovy variable
                        def gradle_home = "C:/gradle"

                        // Export as environment variable
                        env.GRADLE_HOME = gradle_home
                        echo "GRADLE_HOME set to: ${env.GRADLE_HOME}"

                        // Initialize gradleWrapper and nexusRest
                        gradleWrapper = new GradleWrapper(steps: this)
                        nexusRest = new NexusRest(this)
                    }
                }
            }

            stage('Perform Gradle Release') {
                when {
                    allOf {
                        branch 'master'
                        not { triggeredBy 'TimerTrigger' }
                    }
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
                                env.GRADLE_HOME
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
                            gradleWrapper.build(
                                tasks: 'clean build publish printVersion',
                                username: env.NEXUS_USERNAME,
                                password: env.NEXUS_PASSWORD,
                                gradleHome: env.GRADLE_HOME
                            )
                        }
                    }
                }
            }

            stage('Push to Nexus') {
                when { not { triggeredBy 'TimerTrigger' } }
                steps {
                    script {
                        def branch = env.BRANCH_NAME
                        def isMaster = (branch == 'master')
                        def pomLocation = 'build/publications/mavenJava/pom-default.xml'
                        def pom = readMavenPom file: pomLocation
                        def isSnapshot = pom.version.contains('SNAPSHOT')
                        def repoType = isSnapshot ? 'snapshot' : 'release'
                        def nexusRepo = "nexus-${repoType}"
                        def engNexusRepo = "eng-nexus-${repoType}"
                        def shadowJar = true
                        def verbose = false

                        if (isMaster && !isSnapshot) {
                            echo "Uploading to Production Nexus..."
                            nexusRest.uploadReleaseProdNexus(pomLocation, nexusRepo, shadowJar)
                        } else {
                            echo "Uploading to Engineering Nexus..."
                            nexusRest.uploadEngNexusArtifact(
                                pomLocation,
                                engNexusRepo,
                                shadowJar,
                                "${env.NEXUS_USERNAME}:${env.NEXUS_PASSWORD}",
                                verbose
                            )
                        }
                    }
                }
            }

            stage('Push Production Release Tag') {
                when {
                    allOf {
                        branch 'master'
                        not { triggeredBy 'TimerTrigger' }
                        expression { return !skipTagging }
                    }
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
                                gitRepo: git_repo,
                                username: env.GITHUB_USERNAME,
                                password: env.GITHUB_PASSWORD
                            )
                        }
                    }
                }
            }
        }
    }
}
