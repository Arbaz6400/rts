package org.enbd.common


class PipelineBase implements Serializable {
    def steps

    PipelineBase(def steps) {
        this.steps = steps
    }
}
