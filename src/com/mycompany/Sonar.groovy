package com.mycompany

class Sonar extends PipelineBase {
    Sonar(steps) { super(steps) }

    def scan(String sonarToken, String projectKey, String branchName,
             String sources = "src",
             String binaries = "build/classes/java/main",
             String coverage = "build/reports/jacoco/test/jacocoTestReport.xml") {

        if (steps.isUnix()) {
            steps.sh """
                sonar-scanner \
                  -Dsonar.projectKey=${projectKey} \
                  -Dsonar.sources=${sources} \
                  -Dsonar.java.binaries=${binaries} \
                  -Dsonar.host.url=${steps.env.SONARQUBE_URL} \
                  -Dsonar.login=${sonarToken} \
                  -Dsonar.branch.name=${branchName} \
                  -Dsonar.coverage.jacoco.xmlReportPaths=${coverage}
            """
        } else {
            steps.bat """
                sonar-scanner.bat ^
                  -Dsonar.projectKey=${projectKey} ^
                  -Dsonar.sources=${sources} ^
                  -Dsonar.java.binaries=${binaries} ^
                  -Dsonar.host.url=${steps.env.SONARQUBE_URL} ^
                  -Dsonar.login=${sonarToken} ^
                  -Dsonar.branch.name=${branchName} ^
                  -Dsonar.coverage.jacoco.xmlReportPaths=${coverage}
            """
        }
    }
}
