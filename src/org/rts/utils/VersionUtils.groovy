package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps
    def gradleDir = '.'  // default, can be overridden

    VersionUtils(steps, gradleDir = '.') {
        this.steps = steps
        this.gradleDir = gradleDir
    }

    /**
     * Reads the default appVersion from build.gradle robustly
     */
    String getDefaultVersion() {
    def gradleFile = "${gradleDir}/build.gradle"
    if (!steps.fileExists(gradleFile)) {
        steps.echo "⚠️ build.gradle not found at ${gradleFile}"
        return '0.0.1'
    }

    steps.echo "🔍 Reading default version from ${gradleFile}..."
    def content = steps.readFile(gradleFile)
    def lines = content.split('\n')

    for (line in lines) {
        line = line.trim()
        if (line.startsWith('def appVersion') && line.contains('?:')) {
            def parts = line.split('\\?:', 2)
            if (parts.length == 2) {
                // Safe removal of quotes
                def versionPart = parts[1].trim()
                if ((versionPart.startsWith('"') && versionPart.endsWith('"')) ||
                    (versionPart.startsWith("'") && versionPart.endsWith("'"))) {
                    versionPart = versionPart.substring(1, versionPart.length() - 1)
                }
                return versionPart
            }
        }
    }

    steps.echo "⚠️ Could not find appVersion default, returning fallback"
    return '0.0.1'
}

    /**
     * Computes final version based on branch
     */
    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()
        def finalVersion = baseVersion

        if (branchName == 'develop') {
            finalVersion += '-SNAPSHOT'
        } else if (branchName == 'release') {
            finalVersion += '-RC'
        }
        // main/stg -> leave as baseVersion

        return finalVersion
    }
}
