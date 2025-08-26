package org.enbd.common

class NexusRest {

    def steps

    NexusRest(def steps) {
        this.steps = steps
    }

    def uploadReleaseProdNexus(String pom_location, String repository, Boolean shadowJar_plugin, String version = null) {
        def pom = this.steps.readMavenPom(file: pom_location)
        def jar_version = version ?: pom.version

        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${jar_version}-all.jar" :
            "build/libs/${pom.artifactId}-${jar_version}.jar"

        this.steps.echo("Uploading Jar ${jar_location} to ${repository}")

        this.steps.nexusPublisher(
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
                        version: jar_version
                    ]
                ]
            ]
        )
    }
}
