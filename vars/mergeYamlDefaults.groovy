import org.yaml.snakeyaml.Yaml
import com.company.config.DefaultValues

def call() {

    pipeline {
        agent any

        stages {

            stage('Load values.yaml') {
                steps {
                    script {
                        yaml = new Yaml()

                        userValues = fileExists("values.yaml")
                            ? yaml.load(readFile("values.yaml"))
                            : [:]
                    }
                }
            }

            stage('Validate + Merge Defaults') {
                steps {
                    script {
                        DefaultValues.validate(userValues)

                        def defaults = DefaultValues.get()
                        merged = deepMerge(defaults ?: [:], userValues ?: [:])
                    }
                }
            }

            stage('Write Final YAML') {
                steps {
                    script {
                        writeFile file: "values.final.yaml",
                                  text: yaml.dump(merged)

                        echo "Generated values.final.yaml"
                    }
                }
            }
        }
    }
}

/* -------- helper -------- */

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
