def call(Map args) {

    pipeline {
        agent any

        environment {
            ENV = args.env
            CLUSTER = args.cluster
        }

        stages {

            stage('Validate Active Site') {
                steps {
                    script {
                        def ok = org.flink.RedisLogic.isActive(CLUSTER, ENV)
                        if (!ok) {
                            error "Cluster ${CLUSTER} is not active"
                        }
                    }
                }
            }

            stage('Deploy Jobs') {
                steps {
                    echo "Deploying jobs for ${CLUSTER} in ${ENV}"
                    // flink submit logic
                }
            }
        }
    }
}
