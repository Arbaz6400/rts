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
                        // Jenkins-native, CPS-safe
                        def dirs = findFiles(glob: '*')
                            .findAll { it.directory }
                            .collect { it.name }
                            .findAll { it != '.git' }
                            .sort()

                        if (dirs.isEmpty()) {
                            error 'No directories found in repo root'
                        }

                        // store safely in binding
                        binding.LEAP2_DIRS = dirs

                        echo "Found directories: ${dirs.join(', ')}"
                    }
                }
            }

            stage('Select Directory') {
                steps {
                    script {
                        def choiceMap = input(
                            message: 'Select Leap2 directory',
                            ok: 'Proceed',
                            parameters: [
                                choice(
                                    name: 'DIRECTORY',
                                    choices: binding.LEAP2_DIRS.join('\n'),
                                    description: 'Directories from Leap2 repo root'
                                )
                            ]
                        )

                        // input returns Map
                        env.SELECTED_DIR = choiceMap['DIRECTORY']
                        echo "Selected directory: ${env.SELECTED_DIR}"
                    }
                }
            }

            stage('Validate Selection') {
                steps {
                    script {
                        if (!fileExists(env.SELECTED_DIR)) {
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
