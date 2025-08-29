package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Reads default appVersion from build.gradle without regex or split.
     */
    String getDefaultVersion() {
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Reading default version from build.gradle..."
            def content = steps.readFile('build.gradle')
            content.eachLine { line ->
                line = line.trim()
                if (line.startsWith("def appVersion")) {
                    def idx = line.indexOf("?:")
                    if (idx >= 0) {
                        def versionPart = line.substring(idx + 2).trim()
                        // remove quotes if any
                        if ((versionPart.startsWith("'") && versionPart.endsWith("'")) ||
                            (versionPart.startsWith('"') && versionPart.endsWith('"'))) {
                            versionPart = versionPart[1..-2]
                        }
                        return versionPart
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
     * any other branch -> throw error
     */
    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()
        if (branchName == 'develop') {
            return baseVersion + '-SNAPSHOT'
        } else if (branchName == 'main') {
            return baseVersion
        } else {
            steps.error "❌ Branch '${branchName}' is not allowed for versioning."
        }
    }
}
