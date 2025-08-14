node {
    def scriptName = params.PIPELINE_NAME ?: 'build.groovy'

    dir('rts') {
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/main"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/rts.git']]
        ])

        def scriptPath = "${scriptName}"
        if (fileExists(scriptPath)) {
            echo "Loading script from: ${scriptPath}"
            def loadedScript = load(scriptPath)
            loadedScript.executeBuild() // Call custom entry point
        } else {
            error "Script '${scriptPath}' not found in RTS repo"
        }
    }
}
