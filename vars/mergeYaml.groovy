def mergeProgramArgs(Map base, Map override) {
    Map result = [:]

    // Copy everything from base
    base.each { k, v ->
        result[k] = v
    }

    override.each { k, v ->
        if (k == 'programArgs' && v instanceof List) {
            def baseList = result[k] instanceof List ? result[k] : []

            // Convert list of "key=value" strings to map
            def mapBase = [:]
            baseList.each { entry ->
                def (key, value) = entry.split('=', 2)
                mapBase[key] = value
            }

            v.each { entry ->
                def (key, value) = entry.split('=', 2)
                mapBase[key] = value // overwrite or append
            }

            // Convert back to list of strings
            result[k] = mapBase.collect { k, val -> "${k}=${val}" }
        } else if (v instanceof Map && result[k] instanceof Map) {
            result[k] = mergeProgramArgs(result[k], v) // recursive for nested maps
        } else {
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

                        if (!fileExists(baseFile)) error "Base YAML not found: ${baseFile}"
                        if (!fileExists(overrideFile)) error "Override YAML not found: ${overrideFile}"

                        def base = readYaml(file: baseFile) ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        // Only merge programArgs
                        def merged = mergeProgramArgs(base as Map, override as Map)

                        if (fileExists('merged.yaml')) {
                            if (isUnix()) sh 'rm -f merged.yaml' else bat 'del /F merged.yaml'
                        }

                        writeYaml file: 'merged.yaml', data: merged

                        echo "Merged YAML:"
                        if (isUnix()) sh 'cat merged.yaml' else bat 'type merged.yaml'
                    }
                }
            }
        }
    }
}
