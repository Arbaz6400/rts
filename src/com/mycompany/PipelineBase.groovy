package com.mycompany

class PipelineBase implements Serializable {
    def steps
    PipelineBase(steps) { this.steps = steps }
}
