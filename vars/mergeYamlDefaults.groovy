import com.org.config.DefaultValues

def call(Map cfg = [:]) {

    pipeline {
        agent any

        stages {
            stage('Merge YAML Defaults') {
                steps {
                    script {

                        def baseFile   = cfg.base   ?: 'values.yaml'
                        def commonFile = cfg.common ?: 'common-job-config.yaml'

                        if (!fileExists(baseFile)) {
                            error "Base YAML not found: ${baseFile}"
                        }

                        if (!fileExists(commonFile)) {
                            error "Common YAML not found: ${commonFile}"
                        }

                        def baseYaml   = readYaml(file: baseFile)
                        def commonYaml = readYaml(file: commonFile)

                        def defaults = DefaultValues.defaults()

                        def merged = defaults + commonYaml + baseYaml

                        writeYaml file: baseFile, data: merged, overwrite: true

                        echo "Final values.yaml:"

                        // Windows agent -> use bat, not sh
                        bat "type ${baseFile}"
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
