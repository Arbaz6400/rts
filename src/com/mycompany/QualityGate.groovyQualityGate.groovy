package com.mycompany

import groovy.json.JsonSlurper

class QualityGate implements Serializable {

    def steps

    QualityGate(steps) {
        this.steps = steps   // allows us to call sh, echo, error
    }

    void check(String projectKey, String sonarToken, String sonarUrl) {
        def response = steps.sh(
            script: """
                curl -s -u ${sonarToken}: "${sonarUrl}/api/qualitygates/project_status?projectKey=${projectKey}"
            """,
            returnStdout: true
        ).trim()

        def json = new JsonSlurper().parseText(response)
        def status = json.projectStatus.status

        steps.echo "SonarQube Quality Gate Status: ${status}"

        if (status != "OK") {
            steps.error("Quality Gate failed: ${status}")
        }
    }
}
