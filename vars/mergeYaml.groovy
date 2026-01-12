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

                    echo "====== MERGED YAML ======"
                    echo writeYaml(returnText: true, data: merged)

                    writeYaml file: 'merged.yaml', data: merged
                }
            }
        }
    }
}
}
