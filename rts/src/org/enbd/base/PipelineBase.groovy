package org.enbd.base

/**
 * Base class for pipeline steps.
 * Provides common utilities for Jenkins pipelines.
 */
class PipelineBase implements Serializable {

    def steps

    PipelineBase(steps) {
        this.steps = steps
    }

    void log(String message) {
        steps.echo "[PipelineBase] ${message}"
    }
}
