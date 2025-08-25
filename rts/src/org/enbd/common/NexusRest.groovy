package org.enbd.common

import org.enbd.base.PipelineBase

/**
 * Class to interact with Nexus Repository via REST API.
 */
class NexusRest extends PipelineBase {

    NexusRest(steps) {
        super(steps)
    }

    def publish(String groupId, String artifactId, String version, String filePath, String nexusUrl, String username, String password) {
        log("Publishing ${artifactId}-${version}.jar to Nexus")

        steps.sh """
            curl -u ${username}:${password} \
                --upload-file ${filePath} \
                ${nexusUrl}/repository/maven-releases/${groupId.replace('.', '/')}/${artifactId}/${version}/${artifactId}-${version}.jar
        """
    }
}
