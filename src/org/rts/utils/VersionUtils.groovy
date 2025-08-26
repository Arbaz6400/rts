package org.rts.utils

class VersionUtils implements Serializable {

    def steps

    VersionUtils(steps) {
        this.steps = steps
    }

    String getProjectVersion() {
        if (steps.fileExists('pom.xml')) {
            steps.echo "🔍 Found pom.xml, using Maven reader..."
            def pom = steps.readMavenPom file: 'pom.xml'
            return pom.version
        } else if (steps.fileExists('build.gradle')) {
            steps.echo "🔍 Found build.gradle, parsing version..."
            def gradleFile = steps.readFile('build.gradle')
            def version = gradleFile.find(/version\s*=\s*['"](.+)['"]/) { full, ver -> ver }
            if (!version) {
                steps.error "❌ Could not find version in build.gradle"
            }
            return version
        } else {
            steps.error "❌ No pom.xml or build.gradle found in workspace!"
        }
    }
}
