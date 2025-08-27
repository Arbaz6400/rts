package org.enbd.common

import org.enbd.common.PipelineBase

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
            ignoreSslErrors: true
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

    def uploadReleaseProdNexus(String pom_location, String repository, Boolean shadowJar_plugin) {
        def pom = this.steps.readMavenPom(file: pom_location)
        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
            "build/libs/${pom.artifactId}-${pom.version}.jar"

        this.steps.echo("Uploading Jar ${jar_location} to ${repository}")

        this.steps.nexusArtifactUploader(
            nexusVersion: 'nexus3',
            protocol: 'https',
            nexusUrl: 'enqnexus.enbduat.com',
            groupId: pom.groupId,
            version: pom.version,
            repository: repository,
            credentialsId: 'nexus-creds',  // <-- your Jenkins credential ID
            artifacts: [
                [artifactId: pom.artifactId, classifier: '', file: jar_location, type: 'jar'],
                [artifactId: pom.artifactId, classifier: '', file: pom_location, type: 'pom']
            ]
        )
    }

    def uploadReleaseEngNexus(String pom_location, String repository, Boolean shadowJar_plugin) {
        def pom = this.steps.readMavenPom(file: pom_location)
        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
            "build/libs/${pom.artifactId}-${pom.version}.jar"

        this.steps.echo("Uploading Jar ${jar_location} to ${repository}")

        this.steps.nexusArtifactUploader(
            nexusVersion: 'nexus3',
            protocol: 'https',
            nexusUrl: 'enqnexus.enbduat.com',
            groupId: pom.groupId,
            version: pom.version,
            repository: repository,
            credentialsId: 'nexus-creds',
            artifacts: [
                [artifactId: pom.artifactId, classifier: '', file: jar_location, type: 'jar'],
                [artifactId: pom.artifactId, classifier: '', file: pom_location, type: 'pom']
            ]
        )
    }

    def uploadEngNexusArtifact(String pom_location, String repository,
                               Boolean shadowJar_plugin, String userpass, boolean verbose) {
        def pom = this.steps.readMavenPom(file: pom_location)
        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
            "build/libs/${pom.artifactId}-${pom.version}.jar"

        def group_path = pom.groupId.split("\\.").join("/")
        def nexus_reg = "https://enqnexus.enbduat.com/repository/${repository}"

        if (group_path.equals("")) {
            this.steps.error("GroupId not specified, or invalid groupId provided")
        }

        def artifacts = [
            'pom': pom_location,
            'jar': jar_location
        ]

        artifacts.each { artifact, location ->
            this.api_call(
                verbose: verbose,
                httpMode: 'PUT',
                ignoreSslErrors: true,
                authentication: userpass,
                uploadFile: location,
                url: "${nexus_reg}/${group_path}/${pom.artifactId}/${pom.version}/${pom.artifactId}-${pom.version}.${artifact}"
            )
        }
    }

    private def api_call(Map args) {
        def httpMode = args.get('httpMode', 'GET')
        def ignoreSslErrors = args.get('ignoreSslErrors', false) ? "-k" : ""
        def verbose = args.get('verbose', false) ? "-v" : ""
        def url = args['url']
        def authentication = args.get('authentication', '')
        def uploadFile = args.get('uploadFile', '')

        def uploadFileStr = uploadFile ? "--upload-file ${uploadFile}" : ""

        def curl_command = "curl -o /dev/null -w '%{http_code}' ${verbose} ${ignoreSslErrors} -X ${httpMode} ${uploadFileStr} ${url}"

        def curl_result = this.steps.sh(
            script: "${curl_command} -u '${authentication}'",
            returnStdout: true
        ).trim() as Integer

        if (curl_result < 200 || curl_result >= 400) {
            this.steps.error("Upload Failed with code: ${curl_result}")
        }

        return curl_result
    }
}
