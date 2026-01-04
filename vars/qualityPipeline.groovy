
def call() {
    pipeline {
        agent any

        stages {
            stage('Load Secrets') {
                steps {
                    script {
                        def secretsFile = "${env.WORKSPACE}/secrets.yaml"

                        def secrets = org.enbd.rts.SecretLoader.load(
                            this,
                            secretsFile
                        )

                        if (secrets.isEmpty()) {
                            echo "No secrets found for this job"
                        } else {
                            secrets.each { k, v ->
                                echo "Secret loaded for argument: ${k}"
                            }
                        }

                        // You will later pass `secrets` to Flink submit
                    }
                }
            }
        }
    }
}
