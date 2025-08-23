def call() {
    def branch = env.BRANCH_NAME
    def baseVersion = "1.0.0"

    if (branch.startsWith("release/")) {
        baseVersion = branch.split("/")[1]
    } else if (branch == "main") {
        baseVersion = "1.0.0"
    } else if (branch == "develop") {
        baseVersion = "1.1.0"
    }

    def finalVersion
    if (branch == "develop") {
        finalVersion = "${baseVersion}-SNAPSHOT"
    } else if (branch.startsWith("release/")) {
        finalVersion = "${baseVersion}-RC"
    } else if (branch == "stg") {
        finalVersion = baseVersion   // staging strips -RC
    } else if (branch == "main") {
        finalVersion = baseVersion
    } else {
        finalVersion = "${baseVersion}-SNAPSHOT"
    }

    echo "📦 Would set version in build.gradle: ${finalVersion}"
    // simulate sed
    sh """
      sed -i 's/^version = .*/version = "${finalVersion}"/' build.gradle
    """
}
