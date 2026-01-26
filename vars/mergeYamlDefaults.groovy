import com.org.config.DefaultValues

def call() {

    pipeline {
        agent any

        stages {
            stage('Merge YAML Defaults') {
                steps {
                    script {

                        def valuesFile = "values.yaml"

                        if (!fileExists(valuesFile)) {
                            error "values.yaml not found"
                        }

                        // Jenkins provided branch
                        def branch = env.BRANCH_NAME ?: "uat"

                        echo "Detected branch: ${branch}"

                        // Load user yaml
                        def userYaml = readYaml(file: valuesFile) ?: [:]

                        // Load env-specific defaults
                        def defaults = DefaultValues.defaults(branch)

                        // Merge (user overrides defaults)
                        def merged = defaults + userYaml

                        writeYaml file: valuesFile, data: merged, overwrite: true

                        echo "Final merged values.yaml:"

                        // Windows agent
                        bat "type ${valuesFile}"
                    }
                }
            }
        }

        post {
            always {
                cleanWs()
            }
        }
    }
}
