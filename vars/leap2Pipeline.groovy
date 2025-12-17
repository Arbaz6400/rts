def call() {

pipeline {
    agent any

    parameters {
        string(
            name: 'LEAP2_DIR',
            defaultValue: '',
            description: 'Enter root folder name from Leap2 repo'
        )
    }

    environment {
        LEAP2_REPO   = 'https://github.com/Leap-stream/Leap2.git'
        LEAP2_BRANCH = 'main'
    }

    stages {

        stage('Fetch Leap2 Directories') {
            steps {
                script {
                    def tmp = "leap2-${UUID.randomUUID()}"

                    sh """
                      rm -rf ${tmp}
                      git clone --depth 1 -b ${LEAP2_BRANCH} ${LEAP2_REPO} ${tmp}
                    """

                    def dirs = sh(
                        script: "ls -d ${tmp}/*/ 2>/dev/null | xargs -n 1 basename",
                        returnStdout: true
                    ).trim()

                    sh "rm -rf ${tmp}"

                    if (!dirs) {
                        error("No directories found in Leap2 repo")
                    }

                    env.AVAILABLE_DIRS = dirs
                    echo "Available Leap2 directories:\n${dirs}"
                }
            }
        }

        stage('Select Directory (First Run)') {
            when {
                expression { params.LEAP2_DIR.trim() == '' }
            }
            steps {
                error """
No LEAP2_DIR selected.

Available directories:
${env.AVAILABLE_DIRS}

Re-run the job with LEAP2_DIR set.
"""
            }
        }

        stage('Validate Selection') {
            steps {
                script {
                    def validDirs = env.AVAILABLE_DIRS.split('\n')

                    if (!validDirs.contains(params.LEAP2_DIR)) {
                        error """
Invalid directory: ${params.LEAP2_DIR}

Valid options:
${validDirs.join('\n')}
"""
                    }
                }
            }
        }

        stage('Proceed') {
            steps {
                echo "✅ Proceeding with Leap2 directory: ${params.LEAP2_DIR}"

                script {
                    currentBuild.description = "Leap2: ${params.LEAP2_DIR}"
                }
            }
        }
    }
}
}
