node {
    // Parameter to choose which groovy to run
    def scriptName = params.PIPELINE_NAME ?: 'build.groovy'

    echo "Selected pipeline script: ${scriptName}"

    // Checkout the RTS repo in a subfolder
    dir('rts') {
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/main"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/rts.git']]
        ])

        // Full path to the selected Groovy script
        def scriptPath = "${scriptName}"

        if (fileExists(scriptPath)) {
            echo "Loading script from: ${scriptPath}"
            load(scriptPath)
        } else {
            error "Script '${scriptPath}' not found in RTS repo"
        }
    }
}
