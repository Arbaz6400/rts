def call() {

    pipeline {
        agent any

        environment {
            LEAP2_DIRS = ''
            SELECTED_DIR = ''
        }

        stages {

            stage('Fetch Root Directories') {
                steps {
                    script {
                        def root = new File(pwd())
                        def dirs = root.listFiles()
                                      .findAll { it.isDirectory() }
                                      .collect { it.name }
                                      .sort()

                        if (!dirs || dirs.isEmpty()) {
                            error "No directories found in repo root"
                        }

                        env.LEAP2_DIRS = dirs.join(',')
                        echo "Found directories: ${env.LEAP2_DIRS}"
                    }
                }
            }

            stage('Select Directory (First Run Only)') {
                when {
                    expression { !env.SELECTED_DIR?.trim() }
                }
                steps {
                    script {
                        def choice = input(
                            message: 'Select Leap2 directory',
                            ok: 'Proceed',
                            parameters: [
                                choice(
                                    name: 'SELECTED_DIR',
                                    choices: env.LEAP2_DIRS.split(',').join('\n'),
                                    description: 'Directories from Leap2 repo root'
                                )
                            ]
                        )

                        env.SELECTED_DIR = choice
                    }
                }
            }

            stage('Validate Selection') {
                steps {
                    script {
                        def selectedPath = new File("${pwd()}/${env.SELECTED_DIR}")
                        if (!selectedPath.exists()) {
                            error "Selected directory does not exist: ${env.SELECTED_DIR}"
                        }
                        echo "Validated directory: ${env.SELECTED_DIR}"
                    }
                }
            }

            stage('Proceed') {
                steps {
                    echo "Proceeding with directory: ${env.SELECTED_DIR}"
                }
            }
        }
    }
}
