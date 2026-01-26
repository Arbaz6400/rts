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

                        // Read existing values.yaml
                        def userYaml = readYaml(file: valuesFile) ?: [:]

                        // Load defaults from class
                        def defaults = DefaultValues.defaults()

                        /*
                         Map + Map behavior:
                         left overwritten by right
                         So defaults + userYaml keeps user values
                        */
                        def merged = defaults + userYaml

                        // Write back merged yaml
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
