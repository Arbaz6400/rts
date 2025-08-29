package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Reads the default appVersion from build.gradle without using regex.
     * Expects a line like: def appVersion = project.findProperty('appVersion') ?: '1.0.1'
     */
    String getDefaultVersion() {
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Reading default version from build.gradle..."
            def content = steps.readFile('build.gradle')
            def lines = content.split('\n')

            for (line in lines) {
                line = line.trim()
                if (line.startsWith("def appVersion")) {
                    def parts = line.split("\\?:")
                    if (parts.size() == 2) {
                        def versionPart = parts[1].trim()
                        // remove quotes
                        versionPart = versionPart.replaceAll(/^['"]|['"]$/, '')
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
