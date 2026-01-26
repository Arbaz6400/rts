import org.yaml.snakeyaml.Yaml
import com.company.config.DefaultValues

def call() {

    pipeline {
        agent any

        stages {

            stage('Load values.yaml') {
                steps {
                    script {

                        if (!fileExists("values.yaml")) {
                            error "YAML not found: values.yaml"
                        }

                        yaml = new Yaml()

                        userValues = yaml.load(readFile("values.yaml")) ?: [:]

                        echo "Loaded values.yaml successfully"
                    }
                }
            }

            stage('Validate + Merge Defaults') {
                steps {
                    script {

                        // Validate required fields
                        DefaultValues.validate(userValues)

                        def defaults = DefaultValues.get()

                        merged = deepMerge(defaults ?: [:], userValues ?: [:])

                        echo "Defaults merged successfully"
                    }
                }
            }

            stage('Write Final YAML') {
                steps {
                    script {

                        writeFile file: "values.final.yaml",
                                  text: yaml.dump(merged)

                        echo "Generated values.final.yaml"

                        // Optional: show diff
                        sh "diff -u values.yaml values.final.yaml || true"
                    }
                }
            }
        }
    }
}

/* ---------------- Helper: Deep Merge ---------------- */

def deepMerge(Map defaults, Map user) {

    defaults.collectEntries { k, v ->

        if (user.containsKey(k)) {

            if (v instanceof Map && user[k] instanceof Map) {
                [(k): deepMerge(v, user[k])]
            } else {
                [(k): user[k]]
            }

        } else {
            [(k): v]
        }

    } + user.findAll { !defaults.containsKey(it.key) }
}
