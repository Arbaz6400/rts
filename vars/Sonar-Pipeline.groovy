def call(Map cfg = [:]) {
    pipeline {
        agent any
        stages {
            stage('Sonar Scan') {
                steps {
                    script {
                        def sonar = new com.mycompany.Sonar(this)
                        sonar.scan(
                            cfg.token   ?: error("Sonar token missing"),
                            cfg.project ?: (env.JOB_NAME ?: "demo-project"),
                            cfg.branch  ?: (env.BRANCH_NAME ?: "main")
                        )
                    }
                }
            }
            stage('Quality Gate') {
                steps {
                    script {
                        def qg = new com.mycompany.QualityGate(this)
                        qg.check(
                            cfg.token   ?: error("Sonar token missing"),
                            cfg.project ?: (env.JOB_NAME ?: "demo-project"),
                            cfg.branch  ?: (env.BRANCH_NAME ?: "main")
                        )
                    }
                }
            }
        }
    }
}
