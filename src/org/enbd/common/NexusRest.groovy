package org.enbd.common

import org.enbd.base.PipelineBase

/**
 * This class provides methods for interacting with a Nexus repository.
 */
class NexusRest extends PipelineBase {

    NexusRest(def steps) {
        super(steps)
    }

    def getJar(String nexusUrl, String credentialId, String jarName) {
        steps.echo("getJar ${nexusUrl}")
        return steps.httpRequest(
            authentication: credentialId,
            url: nexusUrl,
            consoleLogResponseBody: false,
            outputFile: jarName,
            validResponseCodes: '200',
            ignoreSslErrors: true,
        )
    }

    def getRepositories(String nexusUrl, String username, String password) {
        return """#!/bin/bash -x
        curl -u ${username}:${password} -X GET ${nexusUrl}/service/rest/v1/repositories
        """
    }

    def getRepositoriesList(String nexusUrl, String username, String password) {
        def response = this.getRepositories(nexusUrl, username, password)
        return this.steps.readJSON(text: response)
    }

    def uploadReleaseProdNexus(String pomLocation, String repository, Boolean shadowJarPlugin) {
        def pom = this.steps.readMavenPom(file: pomLocation)
        def jarLocation = shadowJarPlugin ?
                "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
                "build/libs/${pom.artifactId}-${pom.version}.jar"

        this.steps.echo("Uploading Jar ${jarLocation} to ${repository}")
        this.steps.nexusPublisher(
            nexusInstanceId: 'nexus-server',
            nexusRepositoryId: repository,
            packages: [
                [
                    $class: 'MavenPackage',
                    mavenAssetList: [
                        [classifier: "", extension: 'jar', filePath: jarLocation],
                        [classifier: "", extension: 'pom', filePath: pomLocation]
                    ],
                    mavenCoordinate: [
                        artifactId: pom.artifactId,
                        groupId: pom.groupId,
                        packaging: 'jar',
                        version: pom.version,
                    ]
                ]
            ]
        )
    }
}
