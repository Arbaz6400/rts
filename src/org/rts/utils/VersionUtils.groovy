package org.rts.utils

class VersionUtils implements Serializable {

    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    String getProjectVersion() {
        // ✅ Prefer pipeline environment variable if already set
        if (steps.env.APP_VERSION) {
            steps.echo "📌 Using APP_VERSION from pipeline: ${steps.env.APP_VERSION}"
            return steps.env.APP_VERSION
        }

        if (steps.fileExists('pom.xml')) {
            steps.echo "🔍 Found pom.xml, using Maven reader..."
            def pom = steps.readMavenPom file: 'pom.xml'
            return pom.version
        } 
        else if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Found build.gradle, parsing version..."
            def gradleLines = steps.readFile('build.gradle').split("\n")

            String appVersionDefault = null
            String versionValue = null

            for (line in gradleLines) {
                def trimmed = line.trim()

                // capture: def appVersion = project.findProperty('appVersion') ?: '1.0.0'
                if (trimmed.startsWith("def appVersion")) {
                    if (trimmed.contains("?:")) {
                        def fallback = trimmed.split("\\?:")[1].trim()
                        appVersionDefault = fallback.replace("\"", "").replace("'", "")
                    } else if (trimmed.contains("=")) {
                        def parts = trimmed.split("=")
                        if (parts.length == 2) {
                            appVersionDefault = parts[1].trim().replace("\"", "").replace("'", "")
                        }
                    }
                }

                // capture: version = ...
                if (trimmed.startsWith("version")) {
                    def parts = trimmed.split("=")
                    if (parts.length == 2) {
                        versionValue = parts[1].trim()
                                .replace("\"", "")
                                .replace("'", "")
                    }
                }
            }

            // If version points to appVersion, resolve it
            if (versionValue == "appVersion") {
                return appVersionDefault ?: "UNKNOWN"
            }

            // Otherwise, return version as-is
            if (versionValue) {
                return versionValue
            }

            steps.error "❌ Could not determine version from build.gradle"
        } 
        else {
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
