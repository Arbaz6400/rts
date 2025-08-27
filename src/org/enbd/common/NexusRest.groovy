package org.enbd.common

import org.enbd.common.PipelineBase

/**
 * This class provides methods for interacting with a Nexus repository.
 * It extends the PipelineBase class and requires a reference to the pipeline steps.
 */
class NexusRest extends PipelineBase {

    /**
     * Constructor that takes a reference to the pipeline steps.
     */
    NexusRest(def steps) {
        super(steps)
    }

    /**
     * Downloads a JAR file from a Nexus repository.
     * @param nexusUrl The URL of the Nexus repository.
     * @param credentialId The ID of the Jenkins credentials to use.
     * @param jarName The name of the JAR file to download.
     * @return The HTTP response from the download request.
     */
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

    /**
     * Get a list of repositories from a Nexus server.
     */
    def getRepositories(String nexusUrl, String username, String password) {
        return """#!/bin/bash -x
        curl -u ${username}:${password} -X GET ${nexusUrl}/service/rest/v1/repositories
        """
    }

    def getRepositoriesList(String nexusUrl, String username, String password) {
        def response = this.getRepositories(nexusUrl, username, password)
        return this.steps.readJSON(text: response)
    }

    /**
     * Uploads a release to a production Nexus repository using nexusArtifactUploader.
     */
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
            credentialsId: 'nexus-credentials', // replace with your Jenkins credentials ID
            artifacts: [
                [artifactId: pom.artifactId, classifier: '', file: jar_location, type: 'jar'],
                [artifactId: pom.artifactId, classifier: '', file: pom_location, type: 'pom']
            ]
        )
    }

    /**
     * Uploads a release to an engineering Nexus repository.
     */
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
            credentialsId: 'nexus-credentials', // replace with your Jenkins credentials ID
            artifacts: [
                [artifactId: pom.artifactId, classifier: '', file: jar_location, type: 'jar'],
                [artifactId: pom.artifactId, classifier: '', file: pom_location, type: 'pom']
            ]
        )
    }

    /**
     * Uploads an artifact to an engineering Nexus repository using curl.
     */
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
        def form_content = args.get('form_content', [])
        def uploadFile = args.get('uploadFile', '')

        def form_content_str = ""
        form_content.each {
            form_content_str += "-F '${it}' "
        }

        def uploadFileStr = uploadFile ? "--upload-file ${uploadFile}" : ""

        def curl_command = "curl -o /dev/null -w '%{http_code}' ${verbose} ${ignoreSslErrors} -X ${httpMode} ${form_content_str} ${uploadFileStr} ${url}"

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
