package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Returns the project version from build.gradle
     */
    String getProjectVersion() {
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Found build.gradle, parsing version..."
            def text = steps.readFile('build.gradle')
            try {
                def v = parseGradleVersionFromText(text)
                if (v) return v
                steps.error "❌ Could not find version in build.gradle"
            } finally {
                text = null
            }
        }

        steps.error "❌ build.gradle not found in workspace"
    }

    /**
     * Returns version for a specific branch.
     * Currently just returns the standard project version.
     * Can be extended for branch-specific logic.
     */
    String getVersionForBranch(String branch) {
        steps.echo "Getting version for branch: ${branch}"
        return getProjectVersion()
    }

    /**
     * Simple parser for build.gradle versions.
     * Handles:
     *  - version = "1.2.3"
     *  - version '1.2.3'
     *  - version = project.findProperty("version") ?: "1.0.0"
     */
    private String parseGradleVersionFromText(String text) {
        if (!text) return null

        def lines = text.readLines()
        try {
            for (line in lines) {
                if (!line) continue
                def trimmed = line.trim()
                if (!trimmed.toLowerCase().startsWith("version")) continue

                // Remove "version" keyword
                def after = trimmed.replaceFirst("version", "").trim()

                // Remove '=' if present
                if (after.startsWith("=")) after = after.substring(1).trim()

                // Handle fallback: project.findProperty("version") ?: "1.0.0"
                if (after.contains("?:")) {
                    def parts = after.split("\\?:")
                    if (parts.size() > 1) {
                        def fallback = parts[1].trim()
                        if ((fallback.startsWith("\"") && fallback.endsWith("\"")) ||
                            (fallback.startsWith("'") && fallback.endsWith("'"))) {
                            return fallback.substring(1, fallback.length() - 1).trim()
                        }
                        return fallback
                    }
                }

                // Handle simple quoted versions
                if ((after.startsWith("\"") && after.endsWith("\"")) ||
                    (after.startsWith("'") && after.endsWith("'"))) {
                    return after.substring(1, after.length() - 1).trim()
                }

                // Otherwise, first token
                if (after) {
                    def tokens = after.tokenize()
                    if (tokens && tokens[0]) return tokens[0].replaceAll(/[,\;]/, '').trim()
                }
            }
            return null
        } finally {
            lines = null
        }
    }
}
