// Get repo that triggered this run
def parts = env.JOB_NAME.tokenize('/')
def repoName = parts[1]   // e.g. Leap1, Leap2, etc.

properties([
    parameters([
        choice(
            name: 'PIPELINE_TYPE',
            choices: ['build', 'test', 'deploy'],
            description: "Which pipeline to run for ${repoName}?"
        )
    ])
])

node {
    stage('Load Pipeline') {
        echo "Triggered by repo: ${repoName}"
        echo "Selected pipeline: ${params.PIPELINE_TYPE}"

        def scriptPath = "${params.PIPELINE_TYPE}.groovy"
        if (fileExists(scriptPath)) {
            load(scriptPath)
        } else {
            error("Pipeline script ${scriptPath} not found in RTS repo")
        }
    }
}
