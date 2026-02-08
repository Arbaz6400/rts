def call() {
    pipeline {
        agent any

        stages {
            stage('Generate Cluster Jobs') {
                steps {
                    script {
                        generateClusterJobs()
                    }
                }
            }
        }
    }
}

def generateClusterJobs() {
    jobDsl(
  targets: 'dsl/**/*.groovy',
  removedJobAction: 'DELETE',
  removedViewAction: 'DELETE'
)

}

def buildDsl() {
    return """
        import groovy.io.FileType

        def clustersDir = new File("\${new File('.').absolutePath}/clusters")

        if (!clustersDir.exists()) {
            throw new RuntimeException("clusters directory not found in workspace")
        }

        folder('flink-clusters')

        clustersDir.eachFile(FileType.DIRECTORIES) { dir ->
            def clusterName = dir.name

            pipelineJob("flink-clusters/\${clusterName}") {
                description("Auto-generated job for Flink cluster: \${clusterName}")

                definition {
                    cpsScm {
                        scm {
                            git {
                                remote {
                                    url('https://github.com/Leap-stream/Leap2.git')
                                    credentials('github-pat')
                                }
                                branch('develop')
                            }
                        }
                        scriptPath('Jenkinsfile')
                    }
                }
            }
        }
    """
}
