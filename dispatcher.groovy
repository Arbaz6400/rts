// Parameter should be passed from Streaming repo Jenkinsfile
def scriptName = params.PIPELINE_NAME ?: 'build.groovy'

// Always checkout RTS repo into a separate folder so files don't get wiped
dir('rts') {
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/main"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'rts']],
        userRemoteConfigs: [[url: 'https://github.com/Arbaz6400/rts.git']]
    ])

    def scriptPath = "rts/${scriptName}"
    if (fileExists(scriptPath)) {
        echo "Loading RTS script: ${scriptPath}"
        load(scriptPath)
    } else {
        error "Script ${scriptPath} not found in RTS repo"
    }
}
