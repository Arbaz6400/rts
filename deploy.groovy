def executePipeline() {
    stage('Build') {
        echo "Deploying..."
    }
    stage('Test') {
        echo "Running tests..."
    }
}

return this
