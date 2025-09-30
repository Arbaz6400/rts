def call() {
    def versionLine = sh(script: "grep '^version' build.gradle", returnStdout: true).trim()
    def currentVersion = versionLine.split('"')[1]

    def parts = currentVersion.tokenize('.')
    def major = parts[0].toInteger()
    def minor = parts[1].toInteger() + 1
    def newVersion = "${major}.${minor}.0-SNAPSHOT"

    echo "🔼 Would bump develop branch version from ${currentVersion} → ${newVersion}"

    // simulate updating build.gradle
    sh """
      sed -i 's/^version = .*/version = "${newVersion}"/' build.gradle
    """
}
