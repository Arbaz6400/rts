package org.enbd.common

class CheckmarxBase extends PipelineBase {
    def steps

    CheckmarxBase(steps) {
        super(steps)
    }

    // example method placeholder
    def scan() {
        steps.echo "Running Checkmarx scan..."
    }
}
