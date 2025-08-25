package org.enbd.common

class GradleWrapper extends CheckmarxBase {

    private String gradleBinary
    private String gradleHome

    GradleWrapper(steps) {
        super(steps)
        // Always prefer wrapper – works cross-platform
        this.gradleBinary = "./gradlew"
    }

    private def run(String gradle_args, String gradle_tasks, String username, String password, String gradle_home) {
        this.steps.sh """
            export GRADLE_USER_HOME=${gradle_home}
            ${this.gradleBinary} -g ${gradle_home} --stacktrace --no-daemon ${gradle_args} ${gradle_tasks} \
                -PNEXUS_USERNAME=${username} -PNEXUS_PASSWORD=${password}
        """
    }

    public def build(String gradle_args, String gradle_tasks, String username, String password, String gradle_home) {
        this.run(gradle_args, gradle_tasks, username, password, gradle_home)
        this.steps.junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
    }

    public def release(String gradle_args, String gradle_tasks, String nexus_username, String nexus_password, String gradle_home) {
        printGitState()
        this.run(gradle_args, gradle_tasks, nexus_username, nexus_password, gradle_home)
        printGitState()
    }

    def pushTags(String git_repo, String username, String password) {
        this.steps.sh(
            script: "git push https://${username}:${password}@${git_repo} --tags",
            returnStatus: true
        )
    }

    def printGitState() {
        this.steps.sh 'git status'
        this.steps.sh 'git show-ref'
        this.steps.sh 'git tag'
    }
}
