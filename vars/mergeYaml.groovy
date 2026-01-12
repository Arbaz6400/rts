def call(Map cfg = [:]) {

    pipeline {
        agent any

        stages {
            stage('Merge YAMLs') {
                steps {
                    script {
                        // -------- Files --------
                        def baseFile     = cfg.base     ?: 'config/base.yaml'
                        def commonFile   = cfg.common   ?: 'common-job-config.yaml'
                        def overrideFile = cfg.override ?: 'config/override.yaml'

                        [baseFile, commonFile, overrideFile].each { f ->
                            if (!fileExists(f)) {
                                error "YAML not found: ${f}"
                            }
                        }

                        // -------- Read YAML --------
                        def base     = readYaml(file: baseFile)     ?: [:]
                        def common   = readYaml(file: commonFile)   ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        /*
                         * Merge order:
                         * 1️⃣ base
                         * 2️⃣ common
                         * 3️⃣ override (wins)
                         */
                        def merged = deepMerge(base, common)
                        merged     = deepMerge(merged, override)

                        // -------- Write YAML --------
                        writeYaml file: 'merged.yaml', data: merged, overwrite: true

                        // -------- Force quoted programArgs --------
                        forceQuoteProgramArgs('merged.yaml')

                        echo "Merged YAML:"
                        echo readFile('merged.yaml')
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

/* =========================================================
   Helper Functions (OUTSIDE pipeline)
   ========================================================= */

/*
 * Generic deep merge
 * - Maps → recursive merge
 * - programArgs → Flink-specific logic
 * - Everything else → override wins
 */
def deepMerge(Map base, Map override) {
    Map result = [:]
    result.putAll(base)

    override.each { k, v ->

        if (k == 'programArgs'
                && result[k] instanceof List
                && v instanceof List) {

            result[k] = mergeProgramArgs(result[k], v)

        } else if (result[k] instanceof Map && v instanceof Map) {

            result[k] = deepMerge(result[k], v)

        } else {

            result[k] = v
        }
    }

    return result
}

/*
 * Flink programArgs merge
 * Supports:
 *   --key=value  → overridden by key
 *   --flag       → treated as boolean flag
 */
def mergeProgramArgs(List baseArgs, List overrideArgs) {
    Map merged = [:]

    // base first
    baseArgs.each { arg ->
        def clean = arg.replaceFirst(/^--/, '')
        def parts = clean.split('=', 2)
        merged[parts[0]] = parts.size() > 1 ? parts[1] : null
    }

    // override wins
    overrideArgs.each { arg ->
        def clean = arg.replaceFirst(/^--/, '')
        def parts = clean.split('=', 2)
        merged[parts[0]] = parts.size() > 1 ? parts[1] : null
    }

    // rebuild args
    merged.collect { k, v ->
        v == null ? "--${k}" : "--${k}=${v}"
    }
}

/*
 * Ensures YAML output like:
 *   - "--rack-DXB"
 *   - "--key=value"
 */
def forceQuoteProgramArgs(String file) {
    def text = readFile(file)

    text = text.replaceAll(
        /(?m)^- (--.*)$/,
        '- "$1"'
    )

    writeFile file: file, text: text
}
