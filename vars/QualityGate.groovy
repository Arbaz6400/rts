// vars/QualityGate.groovy
import groovy.json.JsonSlurper

class QualityGate implements Serializable {

    static void check(String projectKey, String sonarToken, String SONARQUBE_URL) {
        // Run shell command in Jenkins
        def status = sh(
            script: """
                curl -s -u ${sonarToken}: "${SONARQUBE_URL}/api/qualitygates/project_status?projectKey=${projectKey}" \
                | jq -r '.projectStatus.status'
            """,
            returnStdout: true
        ).trim()

        echo "SonarQube Quality Gate Status: ${status}"

        if (status != "OK") {
            error("Quality Gate failed: ${status}")
        }
    }
}
