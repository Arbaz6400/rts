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
            def gradleLines = steps.readFile('build.gradle').split("\n")

            for (line in gradleLines) {
                def trimmed = line.trim()

                // Case 1: version = '1.0.0'
                if (trimmed.startsWith("version")) {
                    def parts = trimmed.split("=")
                    if (parts.length == 2) {
                        def candidate = parts[1].trim()
                                .replace("\"", "")
                                .replace("'", "")
                        if (candidate) {
                            return candidate
                        }
                    }
                }

                // Case 2: def appVersion = project.findProperty('appVersion') ?: '1.0.0'
                if (trimmed.startsWith("def appVersion")) {
                    if (trimmed.contains("?:")) {
                        def parts = trimmed.split("\\?:")
                        if (parts.length == 2) {
                            def fallback = parts[1].trim()
                                    .replace("\"", "")
                                    .replace("'", "")
                            if (fallback) {
                                return fallback
                            }
                        }
                    } else if (trimmed.contains("=")) {
                        // case like: def appVersion = '1.0.0'
                        def parts = trimmed.split("=")
                        if (parts.length == 2) {
                            def fallback = parts[1].trim()
                                    .replace("\"", "")
                                    .replace("'", "")
                            if (fallback) {
                                return fallback
                            }
                        }
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
        switch (branchName) {
            case "develop":
                return "${baseVersion}-SNAPSHOT"
            case "release":
                return "${baseVersion}-RC"
            default:
                return baseVersion
        }
    }
}
