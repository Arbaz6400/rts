package com.mycompany.quality

class QualityGate implements Serializable {
    def script
    QualityGate(script) {
        this.script = script
    }

    def check(String token, String projectKey, String sonarUrl) {
        script.withCredentials([script.string(credentialsId: token, variable: 'SONAR_AUTH')]) {
            def status = script.sh(
                script: """curl -s -u ${SONAR_AUTH}: \
                    "${sonarUrl}/api/qualitygates/project_status?projectKey=${projectKey}" \
                    | jq -r '.projectStatus.status'""",
                returnStdout: true
            ).trim()

            if (status != 'OK') {
                script.error("Quality Gate failed with status: ${status}")
            } else {
                script.echo "Quality Gate passed ✅"
            }
        }
    }
}
