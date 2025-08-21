package com.mycompany.quality

class QualityGate {
    def script

    QualityGate(script) {
        this.script = script
    }

    def check(String sonarToken, String repoName, String branchName) {
        script.withCredentials([script.string(credentialsId: sonarToken, variable: 'SONAR_AUTH')]) {
            def status = script.sh(
                script: """curl -s -u ${SONAR_AUTH}: \
                    "${script.env.SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${repoName}" \
                    | jq -r '.projectStatus.status'""",
                returnStdout: true
            ).trim()

            if (status != 'OK') {
                script.error("❌ Quality Gate failed for ${repoName}/${branchName} with status: ${status}")
            } else {
                script.echo "✅ Quality Gate passed for ${repoName}/${branchName}"
            }
        }
    }
}
