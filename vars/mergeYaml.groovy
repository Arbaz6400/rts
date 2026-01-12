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
            // append lists
            result[k] = result[k] + v
        }
        else {
            result[k] = v
        }
    }

    return result
}

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

                        // Remove merged.yaml if exists
                        if (fileExists('merged.yaml')) {
                            echo "Removing existing merged.yaml"
                            if (isUnix()) {
                                sh 'rm -f merged.yaml'
                            } else {
                                bat 'del /F merged.yaml'
                            }
                        }

                        // Write the merged YAML
                        writeYaml file: 'merged.yaml', data: merged

                        // Print merged YAML
                        echo "Merged YAML content:"
                        if (isUnix()) {
                            sh 'cat merged.yaml'
                        } else {
                            bat 'type merged.yaml'
                        }
                    }
                }
            }
        }
    }
}
