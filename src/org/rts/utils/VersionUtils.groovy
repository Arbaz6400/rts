package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Reads the default appVersion from build.gradle in a CPS-safe way.
     * Looks for: def appVersion = project.findProperty('appVersion') ?: '1.0.1'
     * Returns the default value (e.g., '1.0.1') or '0.0.1' if not found
     */
    String getDefaultVersion() {
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Reading default version from build.gradle..."
            def content = steps.readFile('build.gradle')
            def lines = content.split('\n')

            for (line in lines) {
                line = line.trim()
                // CPS-safe regex match
                def matcher = line =~ /def\s+appVersion\s*=\s*project\.findProperty\('appVersion'\)\s*\?:\s*['"](.+?)['"]/
                if (matcher) {
                    return matcher[0][1]
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
