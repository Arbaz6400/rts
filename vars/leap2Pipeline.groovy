def call() {

    pipeline {
        agent any

        environment {
            SELECTED_DIR = ''
        }

        stages {

            stage('Fetch Root Directories') {
                steps {
                    script {
                        def ws = pwd()
                        def dirs = []

                        new File(ws).eachDir { d ->
                            dirs << d.name
                        }

                        if (dirs.isEmpty()) {
                            error "No directories found in repo root: ${ws}"
                        }

                        // keep in local variable via binding
                        binding.setVariable('LEAP2_DIR_LIST', dirs.sort())

                        echo "Found directories: ${dirs.join(', ')}"
                    }
                }
            }

            stage('Select Directory (First Run Only)') {
                when {
                    expression { !env.SELECTED_DIR?.trim() }
                }
                steps {
                    script {
                        def dirs = binding.getVariable('LEAP2_DIR_LIST')

                        env.SELECTED_DIR = input(
                            message: 'Select Leap2 directory',
                            ok: 'Proceed',
                            parameters: [
                                choice(
                                    name: 'DIRECTORY',
                                    choices: dirs.join('\n'),
                                    description: 'Directories from Leap2 repo root'
                                )
                            ]
                        )
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
