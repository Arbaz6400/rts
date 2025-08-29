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
            def lines = content.split('\n')
            for (def line : lines) {
                line = line.trim()
                if (line.startsWith("def appVersion")) {
                    def idx = line.indexOf("?:")
                    if (idx >= 0) {
                        def versionPart = line.substring(idx + 2).trim()
                        // CPS-safe quote removal
                        if ((versionPart.length() > 1) &&
                            ((versionPart[0] == "'" && versionPart[-1] == "'") ||
                             (versionPart[0] == '"' && versionPart[-1] == '"'))) {
                            versionPart = versionPart.substring(1, versionPart.length() - 1)
                        }
                        return versionPart
                    }
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
