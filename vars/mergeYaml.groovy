def mergeProgramArgs(Map base, Map override) {
    Map result = [:]

    // Copy everything from base first
    base.each { k, v ->
        result[k] = v
    }

    // Only override programArgs
    override.each { k, v ->
        if (k == 'programArgs' && v instanceof List) {
            def baseList = result[k] instanceof List ? result[k] : []

            // Create a map of base entries using a unique key (you can define what makes an entry unique)
            // Here we assume each entry is a string; if it's an object, you can adjust
            def mergedList = baseList.collect { it }.toList()

            v.each { newEntry ->
                // If exists in base, remove it first
                if (mergedList.contains(newEntry)) {
                    mergedList.remove(newEntry)
                }
                // Then add from override
                mergedList << newEntry
            }

            result[k] = mergedList
        } else if (v instanceof Map && result[k] instanceof Map) {
            result[k] = mergeProgramArgs(result[k], v) // recursive for nested maps if needed
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
