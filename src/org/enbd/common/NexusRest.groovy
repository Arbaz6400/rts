package org.enbd.common

import org.enbd.common.PipelineBase

class NexusRest extends PipelineBase {

    NexusRest(def steps) {
        super(steps)
    }

    def uploadReleaseProdNexus(String pom_location, String repository, Boolean shadowJar_plugin) {
        def pom = this.steps.readMavenPom(file: pom_location)
        def jar_location = shadowJar_plugin ?
            "build/libs/${pom.artifactId}-${pom.version}-all.jar" :
            "build/libs/${pom.artifactId}-${pom.version}.jar"

        this.steps.echo("Uploading Jar ${jar_location} to ${repository}")
        this.steps.nexusPublisher(
            nexusInstanceId: 'nexus-server',
            nexusRepositoryId: repository,
            packages: [[
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
            ]]
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
            this.steps.error("GroupId not specified")
        }

        ['pom': pom_location, 'jar': jar_location].each { artifact, location ->
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
