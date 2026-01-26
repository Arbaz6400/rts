import org.yaml.snakeyaml.Yaml
import com.company.config.DefaultValues

def call() {

    pipeline {
        agent any

        stages {

            stage('Merge YAML Defaults') {
                steps {
                    script {

                        if (!fileExists("values.yaml")) {
                            error "YAML not found: values.yaml"
                        }

                        // Always use def (important for Jenkins CPS)
                        def yaml = new Yaml()

                        def userValues = yaml.load(readFile("values.yaml")) ?: [:]

                        // Validate required fields
                        DefaultValues.validate(userValues)

                        def defaults = DefaultValues.get()

                        def merged = deepMerge(defaults ?: [:], userValues ?: [:])

                        writeFile file: "values.final.yaml",
                                  text: yaml.dump(merged)

                        echo "Generated values.final.yaml"

                        // Optional diff (safe if git-bash exists)
                        try {
                            sh "diff -u values.yaml values.final.yaml || true"
                        } catch (ignored) {
                            echo "Diff skipped (Windows agent)"
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Helper ---------------- */

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
