package org.enbd.common

import org.enbd.base.CheckmarxBase

/**
 * Wrapper class for executing Gradle builds inside Jenkins pipelines.
 */
class GradleWrapper extends CheckmarxBase {

    private String gradleBinary
    private String gradleHome

    GradleWrapper(steps) {
        super(steps)
        this.gradleBinary = "gradle7" // or ./gradlew if bundled
    }

    private def run(String gradleArgs, String gradleTasks, String username, String password, String gradleHome) {
        steps.sh """
            export GRADLE_USER_HOME=${gradleHome}
            ${this.gradleBinary} -g ${gradleHome} --stacktrace --no-daemon ${gradleArgs} ${gradleTasks} \
                -PNEXUS_USERNAME=${username} -PNEXUS_PASSWORD=${password}
        """
    }

    def build(String gradleArgs, String gradleTasks, String username, String password, String gradleHome) {
        this.run(gradleArgs, gradleTasks, username, password, gradleHome)
        this.steps.junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
    }

    def release(String gradleArgs, String gradleTasks, String nexusUsername, String nexusPassword, String gradleHome) {
        printGitState()
        this.run(gradleArgs, gradleTasks, nexusUsername, nexusPassword, gradleHome)
        printGitState()
    }

    def pushTags(String gitRepo, String username, String password) {
        this.steps.sh(
            script: "git push https://${username}:${password}@${gitRepo} --tags",
            returnStatus: true
        )
    }

    def printGitState() {
        this.steps.sh 'git status'
        this.steps.sh 'git show-ref'
        this.steps.sh 'git tag'
    }
}
