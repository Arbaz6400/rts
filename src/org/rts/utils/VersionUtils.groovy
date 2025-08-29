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

            def matcher = content =~ /def\s+appVersion\s*=\s*project\.findProperty\('appVersion'\)\s*\?:\s*['"](.+?)['"]/
            if (matcher) {
                return matcher[0][1]
            }
        }
        return '0.0.1'
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
