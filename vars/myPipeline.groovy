def call() {
    pipeline {
        agent any
        environment {
            NEXUS_USER = credentials('nexus-username')
            NEXUS_PASS = credentials('nexus-password')
        }

        stages {
            stage('Set Version') {
                steps {
                    script {
                        setVersion()   // shared lib function
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        echo "🛠️ Simulating: ./gradlew clean build"
                    }
                }
            }

            stage('Publish') {
                when {
                    not { branch 'main' }  // skip publish on main
                }
                steps {
                    script {
                        echo "👉 Simulating: ./gradlew publish"
                        echo "   Nexus Username: ${NEXUS_USER}"
                        echo "   Nexus Password: (hidden)"
                        echo "   Artifact version: $(grep '^version' build.gradle | cut -d '\"' -f2)"
                    }
                }
            }
        }

        post {
            success {
                script {
                    if (env.BRANCH_NAME == "main") {
                        bumpMinorVersion()   // simulate minor bump in dev
                    }
                }
            }
        }
    }
}
