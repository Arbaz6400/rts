package com.mycompany

class QualityGate extends PipelineBase {
    QualityGate(steps) { super(steps) }

    def check(String sonarToken, String projectKey, String branchName) {
        def status = steps.sh(
            script: """
                curl -s -u ${sonarToken}: \\
                "${steps.env.SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${projectKey}" \\
                | jq -r '.projectStatus.status'
            """,
            returnStdout: true
        ).trim()

        steps.echo "SonarQube Quality Gate status: ${status}"
        if (status != "OK") {
            steps.error("Quality Gate failed: ${status}")
        }
        return status
    }
}
