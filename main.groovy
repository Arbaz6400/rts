properties([
    parameters([
        choice(name: 'PIPELINE_TYPE', choices: ['build', 'test', 'deploy'], description: 'Select pipeline to run')
    ])
])

// Now dynamically load the right pipeline
load "pipelines/${PIPELINE_TYPE}.groovy"
