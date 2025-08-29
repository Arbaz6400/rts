package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    String getDefaultVersion() {
        if (!steps.fileExists('build.gradle')) {
            steps.echo "⚠️ build.gradle not found, using fallback version 0.0.1"
            return '0.0.1'
        }

        steps.echo "🔍 Reading default version from build.gradle..."
        def content = steps.readFile('build.gradle')
        def versionLine = content.readLines().find { it.trim().startsWith("def appVersion") }

        if (versionLine) {
            // Split on ?: to get the fallback part
            def fallbackPart = versionLine.split("\\?:")[-1].trim()
            // Remove quotes if any
            fallbackPart = fallbackPart.replaceAll(/['"]/, "")
            return fallbackPart
        }

        return '0.0.1'
    }

    /**
     * Computes final version based on branch
     * develop -> -SNAPSHOT
     * release -> -RC
     * main -> base version
     * Other branches -> error
     */
    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()
        switch(branchName) {
            case 'develop':
                return baseVersion + '-SNAPSHOT'
            case 'release':
                return baseVersion + '-RC'
            case 'main':
                return baseVersion
            default:
                throw new IllegalArgumentException("Branch ${branchName} not supported for versioning")
        }
    }

}
