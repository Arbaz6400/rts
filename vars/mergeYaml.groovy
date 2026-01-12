def call(Map cfg = [:]) {

    pipeline {
        agent any

        stages {
            stage('Merge YAMLs') {
                steps {
                    script {
                        // ---------- Files ----------
                        def baseFile     = cfg.base     ?: 'config/base.yaml'
                        def commonFile   = cfg.common   ?: 'common-job-config.yaml'
                        def overrideFile = cfg.override ?: 'config/override.yaml'

                        [baseFile, commonFile, overrideFile].each { f ->
                            if (!fileExists(f)) {
                                error "YAML not found: ${f}"
                            }
                        }

                        // ---------- Read YAML ----------
                        def base     = readYaml(file: baseFile)     ?: [:]
                        def common   = readYaml(file: commonFile)   ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        /*
                         * Merge order:
                         * 1️⃣ base
                         * 2️⃣ common → ONLY programArgs
                         * 3️⃣ override → ALL keys allowed
                         */
                        def merged = deepMerge(base, common, false)
                        merged     = deepMerge(merged, override, true)

                        // ---------- Write YAML ----------
                        writeYaml file: 'merged.yaml', data: merged, overwrite: true

                        // ---------- Force quotes for programArgs ----------
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
   Helper Functions (OUTSIDE pipeline – IMPORTANT)
   ========================================================= */

def deepMerge(Map base, Map override, boolean allowAllKeys) {
    Map result = [:]
    result.putAll(base)

    override.each { k, v ->

        // ⛔ Block non-programArgs from common-job-config.yaml
        if (!allowAllKeys && k != 'programArgs') {
            return
        }

        if (k == 'programArgs'
                && result[k] instanceof List
                && v instanceof List) {

            result[k] = mergeProgramArgs(result[k], v)

        } else if (result[k] instanceof Map && v instanceof Map) {

            result[k] = deepMerge(result[k], v, allowAllKeys)

        } else {

            result[k] = v
        }
    }

    return result
}

/*
 * Merges Flink-style args:
 * --key=value  → override by key
 * --flag       → treated as boolean flag
 */
def mergeProgramArgs(List baseArgs, List overrideArgs) {
    Map merged = [:]

    baseArgs.each { arg ->
        def parts = arg.replaceFirst(/^--/, '').split('=', 2)
        merged[parts[0]] = parts.size() > 1 ? parts[1] : null
    }

    overrideArgs.each { arg ->
        def parts = arg.replaceFirst(/^--/, '').split('=', 2)
        merged[parts[0]] = parts.size() > 1 ? parts[1] : null
    }

    // Rebuild args WITHOUT quotes (YAML quoting happens later)
    merged.collect { k, v ->
        v == null ? "--${k}" : "--${k}=${v}"
    }
}

/*
 * Ensures output:
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
