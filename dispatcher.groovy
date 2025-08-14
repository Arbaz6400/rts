node {
    dir('rts') {
        // Checkout RTS repo
        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/main"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/rts.git']]
        ])

        // Read defaults from YAML
        def values = readYaml file: 'values.yaml'
        def defaultScript = values.defaults.pipeline

        // Use param or fallback to YAML default
        def scriptName = params.PIPELINE_NAME ?: defaultScript

        if (fileExists(scriptName)) {
            echo "Loading and executing: ${scriptName}"
            def loadedScript = load(scriptName)
            loadedScript.executePipeline()
        } else {
            error "❌ Script '${scriptName}' not found in RTS repo"
        }
    }
}
