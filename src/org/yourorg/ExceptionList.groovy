package org.yourorg

import hudson.FilePath

class ExceptionList implements Serializable {

    def steps

    ExceptionList(steps) { 
        this.steps = steps
    }

    def call() {
        // Define repo details
        def exceptionsRepo = "https://github.com/Arbaz6400/exception-list.git"
        def exceptionsBranch = "main"
        def exceptionsDir = "exceptions"

        // Checkout exceptions repo inside workspace
        steps.dir(exceptionsDir) {
            steps.checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${exceptionsBranch}"]],
                userRemoteConfigs: [[url: exceptionsRepo]]
            ])
        }

        // Read YAML file
        def exceptionsYaml = "${exceptionsDir}/exceptions.yaml"
        def exceptions = steps.readYaml(file: exceptionsYaml)

        steps.echo "Exceptions → ${exceptions}"

        // Get current repo name from env
        def repo = steps.env.ORG_REPO
        if (exceptions.contains(repo)) {
            steps.echo "Skipping scan for ${repo}"
        } else {
            steps.echo "Running scan for ${repo}"
            // Insert your scan logic here
        }
    }
}
