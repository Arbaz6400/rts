package vars

def call() {
    pipeline {
        agent any

        environment {
            // Nexus credentials stored in Jenkins
            NEXUS = credentials('nexus-creds')

            // Local Gradle home path
            gradle_home = "C:/gradle"

            // Dummy repos
            git_repo = "https://github.com/yourorg/yourrepo.git"
            nexusRepository = "prod-nexus-repo"
            engNexusRepository = "eng-nexus-repo"

            // Gradle build args
            gradle_args = ""
            gradle_tasks = "clean build publish"

            // Control Git tagging
            skipTagging = false
        }

        stages {
            stage('Perform Gradle Release') {
                when {
                    not { triggeredBy 'TimerTrigger' }
                    expression { return env.BRANCH_NAME == 'master' }
                }
                steps {
                    script {
                        def gradleWrapper = new org.enbd.common.GradleWrapper(steps)
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
                                "${NEXUS_USERNAME}",
                                "${NEXUS_PASSWORD}",
                                gradle_home
                            )
                        }
                    }
                }
            }

            stage('Perform Gradle Build') {
                steps {
                    script {
                        def gradleWrapper = new org.enbd.common.GradleWrapper(steps)
                        def shadowJar = true
                        echo "Gradle tasks: ${gradle_tasks}"

                        withCredentials([
                            usernamePassword(
                                credentialsId: 'nexus-creds',
                                usernameVariable: 'NEXUS_USERNAME',
                                passwordVariable: 'NEXUS_PASSWORD'
                            )
                        ]) {
                            gradleWrapper.printGitState()

                            // Capture version from Gradle
                            def versionOutput = sh(
                                script: "${gradle_home}/gradle printVersion -q",
                                returnStdout: true
                            ).trim()
                            echo "Base Version from Gradle: ${versionOutput}"

                            // Append suffix
                            def finalVersion = "${versionOutput}-SNAPSHOT"
                            echo "Version with Suffix: ${finalVersion}"

                            // Store for other stages
                            env.PROJECT_VERSION = finalVersion

                            // Run the build
                            gradleBuild(
                                gradleWrapper,
                                gradle_args,
                                gradle_tasks,
                                "${NEXUS_USERNAME}",
                                "${NEXUS_PASSWORD}",
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
                    unstash 'build'
                    script {
                        def gradleWrapper = new org.enbd.common.GradleWrapper(steps)
                        def nexuskest = new org.enbd.common.NexusRest(steps)

                        def branch = env.BRANCH_NAME
                        def isMaster = (branch == 'master')
                        def pomLocation = 'build/publications/mavenJava/pom-default.xml'
                        def pom = readMavenPom file: pomLocation
                        def versionToUse = env.PROJECT_VERSION ?: pom.version
                        def isSnapshot = versionToUse.contains('SNAPSHOT')
                        def repoType = isSnapshot ? 'snapshot' : 'release'
                        def nexusRepo = "${nexusRepository}-${repoType}"
                        def engNexusRepo = "${engNexusRepository}-${repoType}"
                        def verbose = false

                        if (isMaster && !isSnapshot) {
                            echo "Uploading to Prod Nexus..."
                            nexuskest.uploadReleaseProdNexus(pomLocation, nexusRepo, shadowJar)
                        } else {
                            echo "Uploading to Engineering Nexus..."
                            nexuskest.uploadEngNexusArtifact(
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
                    branch 'master'
                    not { triggeredBy 'TimerTrigger' }
                    expression { return !skipTagging }
                }
                steps {
                    script {
                        def gradleWrapper = new org.enbd.common.GradleWrapper(steps)
                        withCredentials([
                            usernamePassword(
                                credentialsId: 'GITBDMPRODUSR',
                                usernameVariable: 'GITHUB_USERNAME',
                                passwordVariable: 'GITHUB_PASSWORD'
                            )
                        ]) {
                            gradleWrapper.pushTags(
                                git_repo,
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
