private String parseGradleVersionFromText(String text) {
    if (!text) return null

    def lines = text.readLines()
    try {
        for (line in lines) {
            if (!line) continue
            def trimmed = line.trim()

            if (!trimmed.toLowerCase().startsWith("version")) continue

            // Remove "version" word
            def after = trimmed.replaceFirst("version", "").trim()

            // Remove '=' if present
            if (after.startsWith("=")) {
                after = after.substring(1).trim()
            }

            // Handle case: project.findProperty("version") ?: "1.0.0"
            if (after.contains("?:")) {
                def parts = after.split("\\?:")
                if (parts.size() > 1) {
                    def fallback = parts[1].trim()
                    // strip quotes if any
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

            // Otherwise just return the remaining token
            if (after) {
                def tokens = after.tokenize()
                if (tokens && tokens[0]) {
                    return tokens[0].replaceAll(/[,\;]/, '').trim()
                }
            }
        }
        return null
    } finally {
        lines = null
    }
}
