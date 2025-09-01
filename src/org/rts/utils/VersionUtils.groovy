package org.rts.utils

import java.io.Serializable

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Returns the project version from build.gradle, resolving gradle.properties if needed
     */
    String getProjectVersion() {
        if (!steps.fileExists('build.gradle')) {
            steps.error "❌ build.gradle not found in workspace"
        }

        steps.echo "🔍 Found build.gradle, parsing version..."
        def text = steps.readFile('build.gradle')
        try {
            def version = parseGradleVersionFromText(text)
            if (version) return version
            steps.error "❌ Could not determine version from build.gradle"
        } finally {
            text = null
        }
    }

    /**
     * Returns version for a specific branch.
     * Currently just returns the standard project version.
     */
    String getVersionForBranch(String branch) {
        steps.echo "Getting version for branch: ${branch}"
        return getProjectVersion()
    }

    /**
     * Parses build.gradle to find the version.
     * Resolves project.findProperty("prop") using gradle.properties if present.
     * No regex is used.
     */
    private String parseGradleVersionFromText(String text) {
        if (!text) return null

        def lines = text.readLines()
        try {
            for (line in lines) {
                def trimmed = line.trim()
                if (!trimmed.startsWith("version")) continue

                def after = trimmed.replaceFirst("version", "").trim()
                if (after.startsWith("=")) after = after.substring(1).trim()

                // Handle project.findProperty("...") ?: "fallback"
                if (after.startsWith("project.findProperty")) {
                    // extract property name using plain string operations
                    int start = after.indexOf('"') + 1
                    int end = after.indexOf('"', start)
                    def propName = after.substring(start, end)

                    // read gradle.properties
                    if (steps.fileExists('gradle.properties')) {
                        def props = steps.readFile('gradle.properties').readLines()
                        for (p in props) {
                            def lineTrim = p.trim()
                            if (lineTrim.startsWith("${propName}=")) {
                                return lineTrim.split("=")[1].trim()
                            }
                        }
                    }

                    // fallback value after ?: 
                    if (after.contains("?:")) {
                        def fallback = after.split("\\?:")[1].trim()
                        if ((fallback.startsWith("\"") && fallback.endsWith("\"")) ||
                            (fallback.startsWith("'") && fallback.endsWith("'"))) {
                            return fallback.substring(1, fallback.length() - 1)
                        }
                        return fallback
                    }
                }

                // Handle literal version = "1.2.3" or '1.2.3'
                if ((after.startsWith("\"") && after.endsWith("\"")) ||
                    (after.startsWith("'") && after.endsWith("'"))) {
                    return after.substring(1, after.length() - 1).trim()
                }

                // Otherwise, take first token
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
