properties([
    parameters([
        string(name: 'PIPELINE', defaultValue: 'build', description: 'Which pipeline to run (build, test, deploy)')
    ])
])

def pipelineType = params.PIPELINE ?: 'build'

// Map pipeline names to files
def pipelines = [
    'build' : 'build.groovy',
    'test'  : 'test.groovy',
    'deploy': 'deploy.groovy'
]

if (!pipelines.containsKey(pipelineType)) {
    error "Invalid PIPELINE type '${pipelineType}'. Must be one of: ${pipelines.keySet()}"
}

def scriptFile = pipelines[pipelineType]
echo "Loading pipeline: ${scriptFile}"

// Load & execute the chosen pipeline
def scriptContent = readFile(scriptFile)
evaluate(scriptContent)
