package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps
    def gradleDir = '.'  // default folder, can point to Streaming repo if needed

    VersionUtils(steps, gradleDir = '.') {
        this.steps = steps
        this.gradleDir = gradleDir
    }

    /**
     * Reads the default appVersion from build.gradle safely
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
                    def versionPart = parts[1].trim()
                    // remove quotes safely
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
     * Returns final version based on branch
     * Only develop = -SNAPSHOT, main = base version
     * Any other branch throws exception
     */
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
