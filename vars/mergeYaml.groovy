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

                        // ---------- validate ----------
                        [baseFile, commonFile, overrideFile].each { f ->
                            if (!fileExists(f)) {
                                error "YAML not found: ${f}"
                            }
                        }

                        // ---------- load yamls ----------
                        def base     = readYaml(file: baseFile)     ?: [:]
                        def common   = readYaml(file: commonFile)   ?: [:]
                        def override = readYaml(file: overrideFile) ?: [:]

                        // ---------- programArgs only ----------
                        def baseArgs     = (base.programArgs     instanceof List) ? base.programArgs     : []
                        def commonArgs   = (common.programArgs   instanceof List) ? common.programArgs   : []
                        def overrideArgs = (override.programArgs instanceof List) ? override.programArgs : []

                        def mergedProgramArgs = mergeProgramArgs(
                                mergeProgramArgs(baseArgs, commonArgs),
                                overrideArgs
                        )

                        // ---------- final yaml ----------
                        Map finalYaml = [:]
                        finalYaml.putAll(base)              // base structure
                        finalYaml.putAll(override)          // override can add keys
                        finalYaml.programArgs = mergedProgramArgs // enforce rules

                        // ---------- write safely ----------
                        writeYaml file: 'merged.yaml', data: finalYaml, overwrite: true

                        echo "Merged YAML:"
                        echo readFile('merged.yaml')
                    }
                }
            }
        }
    }
}

def deepMerge(Map base, Map override, boolean onlyProgramArgs = false) {
    Map result = [:]
    result.putAll(base)

    override.each { k, v ->
        if (k == 'programArgs'
                && result[k] instanceof List
                && v instanceof List) {

            result[k] = mergeProgramArgs(result[k], v)

        } else if (!onlyProgramArgs) {
            // allow other keys only when not restricted
            result[k] = v
        }
    }
    return result
}

def mergeProgramArgs(List baseArgs, List overrideArgs) {
    Map merged = [:]

    def normalize = { arg ->
        def s = arg.toString().trim()

        // remove surrounding quotes if present
        if ((s.startsWith('"') && s.endsWith('"')) ||
            (s.startsWith("'") && s.endsWith("'"))) {
            s = s[1..-2]
        }

        // remove leading --
        if (s.startsWith('--')) {
            s = s.substring(2)
        }

        return s
    }

    // base first
    baseArgs.each { arg ->
        def s = normalize(arg)
        if (s.contains('=')) {
            def (k, v) = s.split('=', 2)
            merged[k] = v
        } else {
            merged[s] = null
        }
    }

    // override wins
    overrideArgs.each { arg ->
        def s = normalize(arg)
        if (s.contains('=')) {
            def (k, v) = s.split('=', 2)
            merged[k] = v
        } else {
            merged[s] = null
        }
    }

    // output WITH quotes and --
    return merged.collect { k, v ->
        v == null ? "\"--${k}\"" : "\"--${k}=${v}\""
    }
}



