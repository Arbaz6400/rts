def executePipeline() {
    stage('Build') {
        echo "deploy..."
    }
    stage('Test') {
        echo "Running tests..."
    }
}

return this
