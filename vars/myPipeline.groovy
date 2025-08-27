import org.rts.utils.VersionUtils

def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Versioning') {
                steps {
                    script {
                        def versionUtils = new VersionUtils(this)
                        def baseVersion = versionUtils.getProjectVersion()

                        echo "📖 Base version: ${baseVersion}"

                        // Branch-based suffix logic
                        def newVersion = baseVersion
                        if (env.BRANCH_NAME == "develop") {
                            newVersion = "${baseVersion}-SNAPSHOT"
                        } else if (env.BRANCH_NAME == "release") {
                            newVersion = "${baseVersion}-RC"
                        } else if (env.BRANCH_NAME == "main" || env.BRANCH_NAME == "stg") {
                            newVersion = baseVersion
                        }

                        env.APP_VERSION = newVersion
                        echo "📌 Final version: ${env.APP_VERSION}"
                    }
                }
            }
stage('Debug: Find Matchers in Pipeline State') {
    steps {
        script {
            echo "🔎 Scanning pipeline globals for java.util.regex.Matcher..."

            def badVars = []
            binding.variables.each { k, v ->
                if (v instanceof java.util.regex.Matcher) {
                    badVars << "${k} => ${v}"
                }
            }

            if (badVars) {
                echo "❌ Found non-serializable Matchers in pipeline state:"
                badVars.each { echo "   " + it }
                error("Pipeline is holding java.util.regex.Matcher objects, which will break serialization!")
            } else {
                echo "✅ No Matcher objects found in pipeline state."
            }
        }
    }
}

        stage('Upload to Nexus') {
            steps {
                script {
                    // Path to the jar you actually built
            def jarFile = 'build/libs/streaming-1.0.0.jar'
            
            // Verify the jar exists
            if (!fileExists(jarFile)) {
                error "Jar file not found: ${jarFile}"
            } else {
                echo "Uploading Jar ${jarFile} to Nexus releases"
            }

            // Nexus upload using Nexus Artifact Uploader plugin
            nexusArtifactUploader artifacts: [[
                artifactId: 'streaming',
                classifier: '',
                file: jarFile,
                type: 'jar'
            ]],
            credentialsId: 'nexus-creds', // your Jenkins credentials ID
            groupId: 'com.enbd.streaming',
            nexusUrl: 'https://enqnexus.enbduat.com',
            nexusVersion: 'nexus3',
            protocol: 'https',
            repository: 'releases',
            version: '1.0.0'
                }
            }
        }
        }
    }
}

