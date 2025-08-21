package com.mycompany.quality

class QualityGate {
    def script

    QualityGate(script) {
        this.script = script
    }

    def check(String credentialId, String projectKey, String sonarUrl) {
        script.withCredentials([script.string(credentialsId: credentialId, variable: 'SONAR_AUTH')]) {
            def status = script.sh(
                script: """curl -s -u ${SONAR_AUTH}: \
                    "${sonarUrl}/api/qualitygates/project_status?projectKey=${projectKey}" \
                    | jq -r '.projectStatus.status'""",
                returnStdout: true
            ).trim()

            if (status != 'OK') {
                script.error("❌ Quality Gate failed with status: ${status}")
            } else {
                script.echo "✅ Quality Gate passed"
            }
        }
    }
}
