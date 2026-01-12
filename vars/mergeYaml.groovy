def call(Map cfg = [:]) {

    pipeline {
        agent any

        stages {
            stage('Merge YAMLs') {
                steps {
                    script {
                        def baseFile = cfg.base ?: 'config/base.yaml'
                        def overrideFile = cfg.override ?: 'config/override.yaml'

                        echo "Using base: ${baseFile}"
                        echo "Using override: ${overrideFile}"

                        if (!fileExists(baseFile)) {
                            error "Base YAML not found: ${baseFile}"
                        }

                        if (!fileExists(overrideFile)) {
                            error "Override YAML not found: ${overrideFile}"
                        }

                        def base = readYaml(file: baseFile) ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        def merged = deepMerge(base as Map, override as Map)

                        writeYaml file: 'merged.yaml', data: merged

                        echo "Merged YAML:"
                        sh 'cat merged.yaml'
                    }
                }
            }
        }
    }
}
def deepMerge(Map base, Map override) {
    Map result = [:]

    base.each { k, v ->
        result[k] = v
    }

    override.each { k, v ->
        if (result[k] instanceof Map && v instanceof Map) {
            result[k] = deepMerge(result[k], v)
        }
        else if (result[k] instanceof List && v instanceof List) {
            // 👇 list append behavior
            result[k] = result[k] + v
        }
        else {
            result[k] = v
        }
    }
    return result
}
