
// def call(Map params = [:]) {
//     pipeline {
//         agent any

//         environment {
//             // Add any environment variables if needed
//             WORKSPACE_DIR = "${env.WORKSPACE}"
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
//             env.ROOT_DIRS = dirs.join(',')
//                     }
//                 }
//             }

//             stage('Select Directory') {
//     steps {
//         script {
//             def selected = input(
//                 message: 'Select a directory to proceed',
//                 parameters: [
//                     choice(
//                         name: 'DIRECTORY',
//                         choices: env.ROOT_DIRS.tokenize(',').join('\n'),
//                         description: 'Choose one'
//                     )
//                 ]
//             )
//             echo "Selected directory: ${selected}"
//             env.SELECTED_DIR = selected
//         }
//     }
// }

            
//             stage('Validate Selection') {
//                 steps {
//                     script {
//                         if (!env.SELECTED_DIR) {
//                             error "No directory selected"
//                         }
//                     }
//                 }
//             }

//             stage('Proceed') {
//                 steps {
//                     script {
//                         echo "Processing directory: ${env.SELECTED_DIR}"
//                         // Add your build/deploy steps here
//                     }
//                 }
//             }
//         }
//     }
// }




// def call(Map params = [:]) {
//     pipeline {
//         agent any

//         environment {
//             WORKSPACE_DIR = "${env.WORKSPACE}"
//         }

//         stages {
//             stage('Initialize Parameters') {
//                 steps {
//                     script {
//                         // Scan root directories
//                         def raw = bat(
//                             script: '''
//                             @echo off
//                             for /d %%i in (*) do echo %%i
//                             ''',
//                             returnStdout: true
//                         ).trim()

//                         def dirs = raw
//                             .split("\\r?\\n")
//                             .collect { it.trim() }
//                             .findAll { it && it != '.git' }
//                             .sort()

//                         if (dirs.isEmpty()) {
//                             error "No valid directories found in repo root"
//                         }

//                         echo "Found directories: ${dirs.join(', ')}"

//                         // Define a choice parameter dynamically
//                         properties([
//                             parameters([
//                                 choice(
//                                     name: 'SELECTED_DIR',
//                                     choices: dirs.join('\n'),
//                                     description: 'Select directory to build'
//                                 )
//                             ])
//                         ])
//                     }
//                 }
//             }

//             stage('Validate Selection') {
//                 steps {
//                     script {
//                         if (!params.SELECTED_DIR) {
//                             error "No directory selected"
//                         } else {
//                             echo "Selected directory: ${params.SELECTED_DIR}"
//                         }
//                     }
//                 }
//             }

//             stage('Proceed') {
//                 steps {
//                     script {
//                         echo "Processing directory: ${params.SELECTED_DIR}"
//                         // Your build/deploy steps here
//                     }
//                 }
//             }
//         }
//     }
// }


// def call(Map params = [:]) {
//     pipeline {
//         agent any

//         environment {
//             WORKSPACE_DIR = "${env.WORKSPACE}"
//         }

//         stages {
//             stage('Fetch Root Directories') {
//                 steps {
//                     script {
//                         def raw = bat(
//                             script: '''
//                             @echo off
//                             for /d %%i in (*) do echo %%i
//                             ''',
//                             returnStdout: true
//                         ).trim()

//                         def dirs = raw
//                             .split("\\r?\\n")
//                             .collect { it.trim() }
//                             .findAll { it && it != '.git' }
//                             .sort()

//                         if (dirs.isEmpty()) {
//                             error "No valid directories found in repo root"
//                         }

//                         echo "Found directories: ${dirs.join(', ')}"

//                         // Automatically pick the first directory as default
//                         env.SELECTED_DIR = dirs[0]

//                         // Optional: update parameters for UI display in future runs
//                         properties([
//                             parameters([
//                                 choice(
//                                     name: 'SELECTED_DIR',
//                                     choices: dirs.join('\n'),
//                                     description: 'Select directory to build'
//                                 )
//                             ])
//                         ])
//                     }
//                 }
//             }

//             stage('Proceed') {
//                 steps {
//                     script {
//                         echo "Processing directory: ${env.SELECTED_DIR}"
//                         // Build/deploy steps here
//                     }
//                 }
//             }
//         }
//     }
// }


// def call() {
//     pipeline {
//         agent any

//         environment {
//             WORKSPACE_DIR = "${env.WORKSPACE}"
//         }

//         stages {

//             // stage('Update Parameters') {
//             //     steps {
//             //         script {
//             //             // Windows-safe directory listing
//             //             def raw = bat(
//             //                 script: '''
//             //                 @echo off
//             //                 for /d %%i in (*) do echo %%i
//             //                 ''',
//             //                 returnStdout: true
//             //             ).trim()

//             //             // Filter to only actual directories in root
//             //             def dirs = raw
//             //                 .split("\\r?\\n")
//             //                 .collect { it.trim() }
//             //                 .findAll { it && new File("${env.WORKSPACE}\\${it}").isDirectory() && it != '.git' }
//             //                 .sort()

//             //             if (dirs.isEmpty()) {
//             //                 error "No valid directories found in repo root"
//             //             }

//             //             echo "Found directories: ${dirs.join(', ')}"

//             //             // Update the choice parameter dynamically
//             //             properties([
//             //                 parameters([
//             //                     choice(
//             //                         name: 'SELECTED_DIR',
//             //                         choices: dirs.join('\n'),
//             //                         description: 'Select a directory to build'
//             //                     )
//             //                 ])
//             //             ])
//             //         }
//             //     }
//             // }
//                     stage('Update Parameters') {
//     steps {
//         script {
//             // Clean workspace (optional but recommended)
//             deleteDir()

//             // Checkout latest code
//             checkout scm

//             // List only real directories in repo root
//             def dirs = []
//             new File(env.WORKSPACE).eachDir { d ->
//                 if (!d.name.startsWith('.') && d.isDirectory()) {
//                     dirs << d.name
//                 }
//             }

//             if (dirs.isEmpty()) {
//                 error "No valid directories found in repo root"
//             }

//             dirs.sort()
//             echo "Found directories: ${dirs.join(', ')}"

//             // Update choice parameter
//             properties([
//                 parameters([
//                     choice(
//                         name: 'SELECTED_DIR',
//                         choices: dirs.join('\n'),
//                         description: 'Select a directory to build'
//                     )
//                 ])
//             ])
//         }
//     }
// }


//             stage('Proceed') {
//                 steps {
//                     script {
//                         if (!params.SELECTED_DIR) {
//                             error "No directory selected! Please choose one from the parameter."
//                         }
//                         echo "Processing directory: ${params.SELECTED_DIR}"
//                         // Your build/deploy logic here
//                     }
//                 }
//             }

//         }
//     }
// }


pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                // Clean workspace
                deleteDir()
                
                // Checkout SCM
                checkout scm
            }
        }

        stage('Update Choice Parameter') {
            steps {
                script {
                    // Scan only directories in repo root
                    def dirs = []
                    new File(env.WORKSPACE).eachDir { d ->
                        if (!d.name.startsWith('.') && d.isDirectory()) {
                            dirs << d.name
                        }
                    }

                    if (dirs.isEmpty()) {
                        error "No valid directories found in repo root"
                    }

                    dirs.sort()
                    echo "Found directories: ${dirs.join(', ')}"

                    // Update choice parameter dynamically
                    properties([
                        parameters([
                            choice(
                                name: 'SELECTED_DIR',
                                choices: dirs.join('\n'),
                                description: 'Select a directory to build'
                            )
                        ])
                    ])
                }
            }
        }

        stage('Proceed') {
            steps {
                script {
                    echo "Processing directory: ${params.SELECTED_DIR}"
                    // Add your build/deploy steps here
                }
            }
        }
    }
}


