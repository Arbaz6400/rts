package com.mycompany.quality

import groovy.json.JsonSlurper

class QualityGate {
    def script

    QualityGate(script) {
        this.script = script
    }

    def check(String token, String projectKey, String branchName = null) {
        script.echo "🔎 Checking Quality Gate for project: ${projectKey}, branch: ${branchName}"

        def url = "${script.env.SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${projectKey}"
        if (branchName) {
            url += "&branch=${branchName}"
        }

        def response = script.sh(
            script: """curl -s -u ${token}: "${url}" """,
            returnStdout: true
        ).trim()

        def json = new JsonSlurper().parseText(response)
        def status = json.projectStatus.status

        script.echo "✅ Quality Gate Status: ${status}"

        if (status != "OK") {
            script.error("❌ Quality Gate failed with status: ${status}")
        }
    }
}
