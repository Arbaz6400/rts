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
     */
    String getDefaultVersion() {
        if (!steps.fileExists('build.gradle')) {
            return '0.0.1'
        }

        steps.echo "🔍 Reading default version from build.gradle..."
        def lines = steps.readFile('build.gradle').split('\n')

        for (line in lines) {
            line = line.trim()
            if (line.startsWith('def appVersion') && line.contains('?:')) {
                // Split on ?: and remove quotes and spaces
                def parts = line.split('\\?:')
                if (parts.length == 2) {
                    return parts[1].trim().replaceAll("['\"]", "")
                }
            }
        }

        return '0.0.1' // fallback
    }

    /**
     * Computes final version based on branch
     * develop -> -SNAPSHOT
     * release -> -RC
     * main/stg -> base version
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

