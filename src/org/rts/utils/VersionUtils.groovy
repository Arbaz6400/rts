package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps
    def gradleDir

    VersionUtils(steps, gradleDir = '.') {
        this.steps = steps
        this.gradleDir = gradleDir
    }

    // Read appVersion from build.gradle
    String getDefaultVersion() {
        def gradleFile = "${gradleDir}/build.gradle"
        if (!steps.fileExists(gradleFile)) {
            steps.echo "⚠️ build.gradle not found at ${gradleFile}"
            return '0.0.1'
        }

        steps.echo "🔍 Reading default version from ${gradleFile}..."
        def content = steps.readFile(gradleFile)
        content.split('\n').each { line ->
            line = line.trim()
            if (line.startsWith('def appVersion') && line.contains('?:')) {
                def parts = line.split('\\?:', 2)
                if (parts.length == 2) {
                    def versionPart = parts[1].trim()
                    if ((versionPart.startsWith('"') && versionPart.endsWith('"')) ||
                        (versionPart.startsWith("'") && versionPart.endsWith("'"))) {
                        versionPart = versionPart[1..-2] // remove quotes
                    }
                    return versionPart
                }
            }
        }

        steps.echo "⚠️ Could not find appVersion, returning fallback"
        return '0.0.1'
    }

    // Return version based on branch
    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()

        if (branchName == 'develop') {
            return "${baseVersion}-SNAPSHOT"
        } else if (branchName == 'main') {
            return baseVersion
        } else {
            throw new IllegalArgumentException("❌ Unsupported branch '${branchName}'. Only 'develop' and 'main' are allowed.")
        }
    }
}
