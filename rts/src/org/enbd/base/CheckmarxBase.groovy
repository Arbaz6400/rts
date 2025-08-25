package org.enbd.base

class CheckmarxBase extends PipelineBase {
    CheckmarxBase(steps) {
        super(steps)
    }

    void runCheckmarxScan(String projectName) {
        log("Running Checkmarx scan for project: ${projectName}")
    }
}
