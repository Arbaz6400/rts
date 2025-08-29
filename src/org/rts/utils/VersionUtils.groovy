package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Reads the default appVersion from build.gradle
     * Looks for: def appVersion = project.findProperty('appVersion') ?: '1.0.1'
     * Returns the default value (e.g., '1.0.1') or '0.0.1' if not found
     */
    String getDefaultVersion() {
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Reading default version from build.gradle..."
            def content = steps.readFile('build.gradle')
            
            // Simple split-based approach instead of regex/matcher
            content.eachLine { line ->
                line = line.trim()
                if (line.startsWith("def appVersion")) {
                    def parts = line.split("\\?:")
                    if (parts.size() > 1) {
                        return parts[1].trim().replaceAll("['\"]", "")
                    }
                }
            }
        }
        return '0.0.1'
    }

    /**
     * Computes final version based on branch
     * develop -> -SNAPSHOT
     * main -> base version
     * other branches -> throw error
     */
    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()

        if (branchName == 'develop') {
            return baseVersion + '-SNAPSHOT'
        } else if (branchName == 'main') {
            return baseVersion
        } else {
            steps.error("❌ Branch '${branchName}' is not supported for versioning.")
        }
    }
}
