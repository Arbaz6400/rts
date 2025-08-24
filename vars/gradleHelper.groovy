def run(cmd, dryRun = true) {
    if (dryRun) {
        // Just print the gradle command instead of running it
        isUnix() ? sh "echo ./gradlew ${cmd}" : bat "echo gradlew ${cmd}"
    } else {
        // Actually execute gradle
        isUnix() ? sh "./gradlew ${cmd}" : bat "gradlew ${cmd}"
    }
}
