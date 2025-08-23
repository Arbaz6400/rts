def call(String baseVersion = "1.0.0") {
    // Detect branch name from Jenkins or git
    def branch = env.BRANCH_NAME ?: sh(
        script: "git rev-parse --abbrev-ref HEAD",
        returnStdout: true
    ).trim()

    echo "📦 Building from branch: ${branch}"

    def version
    if (branch == "release") {
        version = baseVersion
    } else {
        version = "${baseVersion}-SNAPSHOT"
    }

    // Update build.gradle
    sh """
        sed -i 's/^version = .*/version = "${version}"/' build.gradle
    """

    echo "✅ Using version: ${version}"
    return version
}
