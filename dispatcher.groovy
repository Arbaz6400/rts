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

        if (fileExists(scriptName)) {
            echo "Loading and executing: ${scriptName}"
            def loadedScript = load(scriptName)
            loadedScript.executePipeline() // our custom entry point
        } else {
            error "Script '${scriptName}' not found in RTS repo"
        }
    }
}
