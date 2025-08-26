package org.enbd.common

class NexusRest implements Serializable {
    def steps

    NexusRest(steps) {
        this.steps = steps
    }

    def uploadReleaseProdNexus(String version, String repo) {
        steps.echo "🚀 Uploading artifact to Nexus"
        steps.echo "   → Version: ${version}"
        steps.echo "   → Repository: ${repo}"

        // Replace this with your actual curl/mvn/gradle upload logic
        steps.sh """
            echo "Simulating upload of version ${version} to Nexus repo ${repo}"
        """
    }
}
