package org.enbd.base

class PipelineBase implements Serializable {
    def steps

    PipelineBase(steps) {
        this.steps = steps
    }

    void log(String message) {
        steps.echo "[PipelineBase] ${message}"
    }
}
