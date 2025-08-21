package com.mycompany.quality

class QualityGate implements Serializable {
    def script

    QualityGate(script) {
        this.script = script
    }

    /**
     * Check SonarQube Quality Gate
     * @param sonarToken SonarQube Token
     * @param repoName   Project Key / Repo Name
     * @param branchName Branch Name
     */
    def check(String sonarToken, String repoName, String branchName) {
        script.echo "🔍 Checking Quality Gate for project: ${repoName}, branch: ${branchName}"

        def status = script.sh(
            script: """
                curl -s -u ${sonarToken}: \\
                "${script.env.SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${repoName}" \\
                | jq -r '.projectStatus.status'
            """,
            returnStdout: true
        ).trim()

        script.echo "SonarQube Quality Gate Status: ${status}"

        if (status != 'OK') {
            script.error "❌ Quality Gate failed: ${status}"
        } else {
            script.echo "✅ Quality Gate passed"
        }
    }
}
