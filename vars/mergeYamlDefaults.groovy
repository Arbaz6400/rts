import com.org.config.DefaultValues

def call() {

    pipeline {
        agent any

        stages {
            stage('Merge YAML Defaults') {
                steps {
                    script {

                        def valuesFile = "values.yaml"

                        // Load existing yaml (or empty)
                        Map userValues = [:]

                        if (fileExists(valuesFile)) {
                            userValues = readYaml(file: valuesFile) ?: [:]
                        }

                        // Load defaults from class
                        Map defaults = DefaultValues.defaults()

                        // Merge (user values override defaults)
                        Map merged = mergeMaps(defaults, userValues)

                        // Write back
                        writeYaml file: valuesFile, data: merged, overwrite: true

                        echo "Final values.yaml:"
                        sh "cat ${valuesFile}"
                    }
                }
            }
        }
    }
}

/**
 * Deep merge maps
 */
@NonCPS
Map mergeMaps(Map base, Map override) {
    Map result = [:]
    result.putAll(base)

    override.each { k, v ->
        if (v instanceof Map && result[k] instanceof Map) {
            result[k] = mergeMaps(result[k], v)
        } else {
            result[k] = v
        }
    }

    return result
}
