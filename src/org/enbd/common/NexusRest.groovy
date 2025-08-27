package org.enbd.common

import org.enbd.common.PipelineBase

 

class NexusRest extends PipelineBase {

    NexusRest(def steps) {
        super(steps)
    }

    def uploadReleaseProdNexus(String pom_location, String repository, Boolean shadowJar_plugin) {
        def pom = steps.readMavenPom(file: pom_location)
        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
            "build/libs/${pom.artifactId}-${pom.version}.jar"

        steps.echo("Uploading Jar ${jar_location} to ${repository}")

        steps.nexusPublisher(
            nexusInstanceId: 'nexus-server',
            nexusRepositoryId: repository,
            packages: [
                [
                    $class: 'MavenPackage',
                    mavenAssetList: [
                        [classifier: "", extension: 'jar', filePath: jar_location],
                        [classifier: "", extension: 'pom', filePath: pom_location]
                    ],
                    mavenCoordinate: [
                        artifactId: pom.artifactId,
                        groupId: pom.groupId,
                        packaging: 'jar',
                        version: pom.version
                    ]
                ]
            ]
        )
    }
}
