package org.rts.utils

class VersionUtils implements Serializable {
    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    /**
     * Gets the project version from (in order):
     *  - pom.xml (using readMavenPom if available)
     *  - pom-temp.xml (if exists and you modify the pom mid-pipeline)
     *  - build.gradle (by scanning lines)
     */
    String getProjectVersion() {
        // prefer pom.xml via maven reader (safe)
        if (steps.fileExists('pom.xml')) {
            steps.echo "🔍 Found pom.xml, using Maven reader..."
            def pom = steps.readMavenPom file: 'pom.xml'
            def v = pom?.version
            pom = null
            return v
        }

        // check a modified pom-temporary file many pipelines use
        if (steps.fileExists('pom-temp.xml')) {
            steps.echo "🔍 Found pom-temp.xml, scanning for <version>..."
            def content = steps.readFile('pom-temp.xml')
            try {
                def v = extractXmlTagValue(content, 'version')
                return v
            } finally {
                content = null
            }
        }

        // fallback: build.gradle (simple line scanning, no regex Matcher)
        if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Found build.gradle, scanning for version..."
            def text = steps.readFile('build.gradle')
            try {
                def v = parseGradleVersionFromText(text)
                if (v) return v
                steps.error "❌ Could not find version in build.gradle"
            } finally {
                text = null
            }
        }

        steps.error "❌ No pom.xml, pom-temp.xml or build.gradle found in workspace!"
    }

    // --- helper: extract XML tag content using indexOf/subSequence (no regex)
    private String extractXmlTagValue(String xmlText, String tagName) {
        if (!xmlText) return null
        def open = "<${tagName}>"
        def close = "</${tagName}>"
        def iOpen = xmlText.indexOf(open)
        if (iOpen == -1) return null
        iOpen += open.length()
        def iClose = xmlText.indexOf(close, iOpen)
        if (iClose == -1) return null
        def value = xmlText.substring(iOpen, iClose).trim()
        return value ?: null
    }

    // --- helper: parse gradle version lines without using =~
    private String parseGradleVersionFromText(String text) {
        if (!text) return null
        def lines = text.readLines()
        try {
            // find common patterns: "version = '1.2.3'" or "version = \"1.2.3\"" or "version '1.2.3'"
            for (line in lines) {
                if (!line) continue
                def trimmed = line.trim()
                if (!trimmed.startsWith("version")) continue

                // examples:
                // version = '1.0.0'
                // version = "1.0.0"
                // version '1.0.0'
                // version "1.0.0"
                def after = trimmed - 'version'
                after = after.trim()
                // If there's an equals sign, remove it
                if (after.startsWith("=")) {
                    after = after.substring(1).trim()
                }
                // now after should be something like "'1.0.0'" or '"1.0.0"' or " '1.0.0' "
                // remove surrounding quotes if present
                if (after.startsWith("'") && after.endsWith("'") && after.length() >= 2) {
                    return after.substring(1, after.length() - 1).trim()
                }
                if (after.startsWith('"') && after.endsWith('"') && after.length() >= 2) {
                    return after.substring(1, after.length() - 1).trim()
                }
                // sometimes there is no quoting but the version token is present
                if (after) {
                    // token split to pick first token
                    def tokens = after.tokenize()
                    if (tokens && tokens[0]) {
                        // strip any trailing characters like commas
                        def candidate = tokens[0].replaceAll(/[,\;]/, '').trim()
                        if (candidate) return candidate
                    }
                }
            }
            return null
        } finally {
            // explicitly nil out list reference
            lines = null
        }
    }
}
