def call() {

pipeline {
    agent any

    stages {

        stage('Merge YAMLs') {
            steps {
                script {
                    def baseFile = 'config/base.yaml'
                    def overrideFile = 'config/override.yaml'

                    if (!fileExists(baseFile)) {
                        error "Base YAML not found: ${baseFile}"
                    }

                    if (!fileExists(overrideFile)) {
                        error "Override YAML not found: ${overrideFile}"
                    }

                    def base = readYaml(file: baseFile) ?: [:]
                    def override = readYaml(file: overrideFile) ?: [:]

                    def merged = deepMerge(base as Map, override as Map)

                    echo "Merged YAML:"
                    echo merged.toString()

                    writeYaml file: 'merged.yaml', data: merged
                }
            }
        }
    }
}
}

/* --------- HELPER FUNCTIONS (OUTSIDE pipeline) --------- */

def deepMerge(Map base, Map override) {
    Map result = [:]

    base.each { k, v ->
        result[k] = v
    }

    override.each { k, v ->
        if (result[k] instanceof Map && v instanceof Map) {
            result[k] = deepMerge(result[k], v)
        } else {
            result[k] = v
        }
    }
    return result
}
