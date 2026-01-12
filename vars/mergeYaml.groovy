def call(Map cfg = [:]) {

    pipeline {
        agent any

        stages {
            stage('Merge YAMLs') {
                steps {
                    script {
                        def baseFile     = cfg.base     ?: 'config/base.yaml'
                        def commonFile   = cfg.common   ?: 'common-job-config.yaml'
                        def overrideFile = cfg.override ?: 'config/override.yaml'

                        [baseFile, commonFile, overrideFile].each { f ->
                            if (!fileExists(f)) {
                                error "YAML not found: ${f}"
                            }
                        }

                        def base     = readYaml(file: baseFile)     ?: [:]
                        def common   = readYaml(file: commonFile)   ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        def merged = deepMerge(base, common)
                        merged = deepMerge(merged, override)

                        writeYaml file: 'merged.yaml',
                                  data: merged,
                                  overwrite: true

                        echo "Merged YAML:"
                        echo readFile('merged.yaml')
                    }
                }
            }
        }

        post {
            always {
                echo "Cleaning workspace"
                cleanWs()
            }
        }
    }
}

/* =========================
   Helper functions
   ========================= */

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

def mergeProgramArgs(List baseArgs, List overrideArgs) {
    Map kv = [:]
    List flags = []

    def process = { arg ->
        if (arg.contains('=')) {
            def parts = arg.split('=', 2)
            kv[parts[0]] = parts[1]
        } else {
            flags << arg
        }
    }

    // base first
    baseArgs.each(process)

    // override wins
    overrideArgs.each(process)

    // rebuild final list
    List result = []
    kv.each { k, v -> result << "${k}=${v}" }
    result.addAll(flags.unique())

    return result
}
