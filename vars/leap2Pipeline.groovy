
// def call() {
//     pipeline {
//         agent any

//         environment {
//             SELECTED_DIRECTORY = ''
//         }

//         stages {

//             stage('Fetch Root Directories') {
//                 steps {
//                     script {
//                         // Windows-safe directory listing
//                         def raw = bat(
//                             script: '''
//                             @echo off
//                             for /d %%i in (*) do echo %%i
//                             ''',
//                             returnStdout: true
//                         ).trim()

//                         if (!raw) {
//                             error "No directories found in repo root"
//                         }

//                         // Filter and sort directories
//                         def dirs = raw
//                             .split("\\r?\\n")
//                             .collect { it.trim() }
//                             .findAll { it && it != '.git' }
//                             .sort()

//                         if (dirs.isEmpty()) {
//                             error "No valid directories found in repo root"
//                         }

//                         echo "Found directories: ${dirs.join(', ')}"

//                         // Save dirs in env for next stage
//                         env.DIRS_LIST = dirs.join('\n')
//                     }
//                 }
//             }

//   stage('Select Directory') {
//     steps {
//         script {
//             // Example directories list
//             def dirs = ['exceptions', 'scripts', 'scripts2']

//             // input returns either string or map
//             def userInput = input(
//                 message: 'Select directory to proceed',
//                 ok: 'Continue',
//                 parameters: [
//                     choice(
//                         name: 'DIRECTORY',
//                         choices: dirs.join('\n'),
//                         description: 'Root directory'
//                     )
//                 ]
//             )

//             // Capture the selected directory properly
//             def selectedDir = (userInput instanceof Map) ? userInput['DIRECTORY'] : userInput
//             echo "Selected directory: ${selectedDir}"

//             // Save to environment for later stages
//             env.SELECTED_DIRECTORY = selectedDir
//         }
//     }
// }


//             stage('Validate Selection') {
//                 steps {
//                     script {
//                         if (!env.SELECTED_DIRECTORY) {
//                             error "No directory selected"
//                         }
//                     }
//                 }
//             }

//             stage('Proceed') {
//                 steps {
//                     echo "Proceeding with directory: ${env.SELECTED_DIRECTORY}"
//                 }
//             }
//         }
//     }
// }


// def selectedDir = null  // define outside stages

// pipeline {
//     agent any
//     stages {
//         stage('Fetch Root Directories') {
//             steps {
//                 script {
//                     def dirs = ['exceptions', 'scripts', 'scripts2']
//                     echo "Found directories: ${dirs.join(', ')}"
//                 }
//             }
//         }
//         stage('Select Directory') {
//             steps {
//                 script {
//                     def dirs = ['exceptions', 'scripts', 'scripts2']
//                     def userInput = input(
//                         message: 'Select directory to proceed',
//                         ok: 'Continue',
//                         parameters: [
//                             choice(name: 'DIRECTORY', choices: dirs.join('\n'), description: 'Root directory')
//                         ]
//                     )
//                     // Capture input properly
//                     selectedDir = (userInput instanceof Map) ? userInput['DIRECTORY'] : userInput
//                     echo "Selected directory: ${selectedDir}"
//                 }
//             }
//         }
//         stage('Validate Selection') {
//             steps {
//                 script {
//                     if (!selectedDir) {
//                         error "No directory selected"
//                     } else {
//                         echo "Directory validated: ${selectedDir}"
//                     }
//                 }
//             }
//         }
//         stage('Proceed') {
//             steps {
//                 script {
//                     echo "Proceeding with ${selectedDir}"
//                 }
//             }
//         }
//     }
// }

// vars/leap2Pipeline.groovy

def call(Map params = [:]) {
    pipeline {
        agent any

        environment {
            // Add any environment variables if needed
            WORKSPACE_DIR = "${env.WORKSPACE}"
        }

        stages {
//             stage('Fetch Root Directories') {
//     steps {
//         script {
//             // List directories in the repo root
//             def dirs = findFiles(glob: '*', type: 'DIRECTORY').collect { it.name }
//             echo "Found directories: ${dirs.join(', ')}"

//             if (dirs.isEmpty()) {
//                 error "No directories found in repo root!"
//             }

//             // Save for later input selection
//             env.ROOT_DIRS = dirs.join(',')
//         }
//     }
// }

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

