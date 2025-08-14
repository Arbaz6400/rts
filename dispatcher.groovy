// dispatcher.groovy
def runPipeline(defaultScript) {
    node {
        // Use param if passed, else default from streaming repo Jenkinsfile
        def scriptName = params.PIPELINE_NAME ?: defaultScript

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
                loadedScript.executePipeline()
            } else {
                error "❌ Script '${scriptName}' not found in RTS repo"
            }
        }
    }
}

return this
