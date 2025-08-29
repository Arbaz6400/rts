package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    String getDefaultVersion() {
    if (steps.fileExists('build.gradle')) {
        steps.echo "🔍 Reading default version from build.gradle..."
        def content = steps.readFile('build.gradle')
        def line = content.readLines().find { it.contains("def appVersion") }
        if (line) {
            // Extract version between quotes
            def matcher = line =~ /['"](.+?)['"]/
            if (matcher) {
                return matcher[0][1]  // Just the version without quotes
            }
        }
    }
    return '0.0.1'
}

    String getVersionForBranch(String branchName) {
        def baseVersion = getDefaultVersion()
        if ("develop".equals(branchName)) {
            return baseVersion + "-SNAPSHOT"
        } else if ("main".equals(branchName)) {
            return baseVersion
        } else {
            steps.error "❌ Branch '${branchName}' is not allowed for versioning."
        }
    }
}
