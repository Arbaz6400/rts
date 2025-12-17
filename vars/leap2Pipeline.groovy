


// def call() {

//     pipeline {
//         agent any

//         environment {
//             SELECTED_DIR = ''
//         }

//         stages {

//             stage('Fetch Root Directories') {
//     steps {
//         script {
//             def output = bat(
//                 script: '''
//                 for /d %%i in (*) do @echo %%i
//                 ''',
//                 returnStdout: true
//             ).trim()

//             if (!output) {
//                 error "No directories found in repo root"
//             }

//             def dirs = output
//                 .split("\\r?\\n")
//                 .collect { it.trim() }
//                 .findAll { it && it != '.git' }
//                 .sort()

//             if (dirs.isEmpty()) {
//                 error "No valid directories found in repo root"
//             }

//             binding.LEAP2_DIRS = dirs
//             echo "Found directories: ${dirs.join(', ')}"
//         }
//     }
// }



//             stage('Select Directory') {
//                 steps {
//                     script {
//                         def choiceMap = input(
//                             message: 'Select Leap2 directory',
//                             ok: 'Proceed',
//                             parameters: [
//                                 choice(
//                                     name: 'DIRECTORY',
//                                     choices: binding.LEAP2_DIRS.join('\n'),
//                                     description: 'Directories from Leap2 repo root'
//                                 )
//                             ]
//                         )

//                         // input returns Map
//                         env.SELECTED_DIR = choiceMap['DIRECTORY']
//                         echo "Selected directory: ${env.SELECTED_DIR}"
//                     }
//                 }
//             }

//             stage('Validate Selection') {
//                 steps {
//                     script {
//                         if (!fileExists(env.SELECTED_DIR)) {
//                             error "Selected directory does not exist: ${env.SELECTED_DIR}"
//                         }
//                         echo "Validated directory: ${env.SELECTED_DIR}"
//                     }
//                 }
//             }

//             stage('Proceed') {
//                 steps {
//                     echo "Proceeding with directory: ${env.SELECTED_DIR}"
//                 }
//             }
//         }
//     }
// }



def call() {

    def dirs = []

    stage('Fetch Root Directories') {
        script {
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

            dirs = raw
                .split("\\r?\\n")
                .collect { it.trim() }
                .findAll { it && it != '.git' }
                .sort()

            if (dirs.isEmpty()) {
                error "No valid directories found in repo root"
            }

            echo "Found directories: ${dirs.join(', ')}"
        }
    }

    stage('Select Directory') {
        script {
            def userInput = input(
                message: 'Select directory to proceed',
                ok: 'Continue',
                parameters: [
                    choice(
                        name: 'DIRECTORY',
                        choices: dirs.join('\n'),
                        description: 'Root directory'
                    )
                ]
            )

            // ✅ input returns a MAP
            env.SELECTED_DIRECTORY = userInput['DIRECTORY']
            echo "Selected directory: ${env.SELECTED_DIRECTORY}"
        }
    }

    stage('Validate Selection') {
        script {
            if (!env.SELECTED_DIRECTORY) {
                error "No directory selected"
            }
        }
    }

    stage('Proceed') {
        echo "Proceeding with directory: ${env.SELECTED_DIRECTORY}"
        // your logic here
    }
}
