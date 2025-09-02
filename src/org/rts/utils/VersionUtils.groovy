package org.rts.utils

class VersionUtils implements Serializable {

    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    String getProjectVersion() {
        if (steps.fileExists('pom.xml')) {
            steps.echo "🔍 Found pom.xml, using Maven reader..."
            def pom = steps.readMavenPom file: 'pom.xml'
            return pom.version

        } else if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Found build.gradle, parsing version..."
            def gradleFile = steps.readFile('build.gradle').split("\n")

            for (line in gradleFile) {
                line = line.trim()

                // Case 1: direct version = 'x.y.z'
                if (line.startsWith("version")) {
                    def parts = line.split("=")
                    if (parts.length == 2) {
                        def versionCandidate = parts[1].trim().replaceAll("['\"]", "")
                        if (versionCandidate) {
                            return versionCandidate
                        }
                    }
                }

                // Case 2: def appVersion = project.findProperty('appVersion') ?: '1.0.1'
                if (line.startsWith("def appVersion")) {
                    def defaultMatch = (line =~ /['"]([^'"]+)['"]$/)
                    if (defaultMatch) {
                        def fallbackVersion = defaultMatch[0][1]
                        return fallbackVersion
                    }
                }
            }

            steps.error "❌ Could not determine version from build.gradle"

        } else {
            steps.error "❌ No pom.xml or build.gradle found in workspace!"
        }
    }
    String getVersionForBranch(String branchName) {
    def baseVersion = getProjectVersion()
    switch(branchName) {
        case "develop":
            return "${baseVersion}-SNAPSHOT"
        case "release":
            return "${baseVersion}-RC"
        default:
            return baseVersion
    }
}

}
