def executePipeline() {
    stage('Build') {
        echo "Building..."
    }
    stage('Test') {
        echo "Running tests..."
    }
}

return this
