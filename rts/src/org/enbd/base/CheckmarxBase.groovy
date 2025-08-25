package org.enbd.base

/**
 * Base class for Checkmarx scanning functionality.
 */
class CheckmarxBase extends PipelineBase {

    CheckmarxBase(steps) {
        super(steps)
    }

    void runCheckmarxScan(String projectName) {
        log("Running Checkmarx scan for project: ${projectName}")
        // Placeholder: integrate with Checkmarx Jenkins plugin or API
    }
}
