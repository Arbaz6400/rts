def call() {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        def branch = env.BRANCH_NAME
                        def buildNum = env.BUILD_NUMBER
                        def baseVersion = readBaseVersion()

                        def version = computeVersion(branch, buildNum, baseVersion)
                        echo "📌 Using version: ${version}"
                        env.PROJECT_VERSION = version
                    }
                }
            }

            stage('Build') {
                steps {
                    echo "🛠️ Simulating Gradle build..."
                    // later: gradleCmd("clean build")
                }
            }

            stage('Publish') {
                when { expression { env.BRANCH_NAME == 'release' || env.BRANCH_NAME == 'main' } }
                steps {
                    echo "🚀 Would publish ${env.PROJECT_VERSION} to Nexus"
                    // nexusPush(env.PROJECT_VERSION)
                }
            }

            stage('Show Version') {
                steps {
                    echo "✅ Final version: ${env.PROJECT_VERSION}"
                }
            }
        }
    }
}

def readBaseVersion() {
    def matcher = readFile('build.gradle') =~ /version\s*=\s*"(.*)"/
    return matcher ? matcher[0][1] : "0.0.1"
}

def computeVersion(branch, buildNum, baseVersion) {
    if (branch == 'develop') {
        return "${baseVersion}-SNAPSHOT"
    } else if (branch == 'release') {
        return "${baseVersion}-RC"
    } else if (branch == 'main' || branch == 'stg') {
        return baseVersion
    } else {
        return "${baseVersion}-${branch}-${buildNum}"
    }
}
