
def call(Map params = [:]) {
    pipeline {
        agent any

        environment {
            // Add any environment variables if needed
            WORKSPACE_DIR = "${env.WORKSPACE}"
        }

        stages {

            stage('Fetch Root Directories') {
                steps {
                    script {
                        // Windows-safe directory listing
                        def raw = bat(
                            script: '''
                            @echo off
                            for /d %%i in (*) do echo %%i
                            ''',
                            returnStdout: true
                        ).trim()

                        if (!raw) {
                            error "No directories found in repo root"
                        }

                        // Filter and sort directories
                        def dirs = raw
                            .split("\\r?\\n")
                            .collect { it.trim() }
                            .findAll { it && it != '.git' }
                            .sort()

                        if (dirs.isEmpty()) {
                            error "No valid directories found in repo root"
                        }

                        echo "Found directories: ${dirs.join(', ')}"

                        // Save dirs in env for next stage
            env.ROOT_DIRS = dirs.join(',')
                    }
                }
            }

            stage('Select Directory') {
    steps {
        script {
            def selected = input(
                message: 'Select a directory to proceed',
                parameters: [
                    choice(
                        name: 'DIRECTORY',
                        choices: env.ROOT_DIRS.tokenize(',').join('\n'),
                        description: 'Choose one'
                    )
                ]
            )
            echo "Selected directory: ${selected}"
            env.SELECTED_DIR = selected
        }
    }
}

            
            stage('Validate Selection') {
                steps {
                    script {
                        if (!env.SELECTED_DIR) {
                            error "No directory selected"
                        }
                    }
                }
            }

            stage('Proceed') {
                steps {
                    script {
                        echo "Processing directory: ${env.SELECTED_DIR}"
                        // Add your build/deploy steps here
                    }
                }
            }
        }
    }
}

