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

/* =====================================================
   Helper functions (MUST be outside pipeline)
   ===================================================== */
def mergeProgramArgs(List baseArgs, List overrideArgs) {
    Map merged = [:]

    def normalize = { String arg ->
        arg.startsWith('--') ? arg.substring(2) : arg
    }

    // base first
    baseArgs.each { arg ->
        def clean = normalize(arg)

        if (clean.contains('=')) {
            def (k, v) = clean.split('=', 2)
            merged[k] = v
        } else {
            merged[clean] = null
        }
    }

    // override wins
    overrideArgs.each { arg ->
        def clean = normalize(arg)

        if (clean.contains('=')) {
            def (k, v) = clean.split('=', 2)
            merged[k] = v
        } else {
            merged[clean] = null
        }
    }

    // rebuild args WITH --
   merged.collect { k, v ->
    v == null ? "\"--${k}\"" : "\"--${k}=${v}\""
}

}
