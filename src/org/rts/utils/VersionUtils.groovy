package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Returns the project version.
     * Checks gradle.properties first. Falls back to default.
     */
    String getProjectVersion() {
        def defaultVersion = "1.0.0"

        if (steps.fileExists('gradle.properties')) {
            def props = steps.readFile('gradle.properties').readLines()
            for (line in props) {
                def trimmed = line.trim()
                if (trimmed && trimmed.startsWith("version=")) {
                    def value = trimmed.split("=")[1].trim()
                    steps.echo "📌 Found version in gradle.properties: ${value}"
                    return value
                }
            }
        }

        // fallback if gradle.properties missing or version not defined
        steps.echo "⚠️ version not found in gradle.properties, using default: ${defaultVersion}"
        return defaultVersion
    }

    /**
     * For branch-specific logic (optional)
     */
    String getVersionForBranch(String branch) {
        def baseVersion = getProjectVersion()
        // optionally append -SNAPSHOT for non-main branches
        if (branch != "main" && !branch.endsWith("-SNAPSHOT")) {
            return "${baseVersion}-SNAPSHOT"
        }
        return baseVersion
    }
}
