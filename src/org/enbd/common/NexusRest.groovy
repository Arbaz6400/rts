// src/org/enbd/common/NexusRest.groovy
package org.enbd.common

class NexusRest implements Serializable {
    def steps

    NexusRest(steps) {
        this.steps = steps
    }

    def uploadReleaseProdNexus(String version, String repo) {
        steps.echo "🚀 Uploading artifact to Nexus"
        steps.echo "   → Repository: ${repo}"
        steps.echo "   → Version: ${version}"

        // Example: Simulate Nexus upload (replace with actual Nexus commands)
        steps.sh """
            echo "Simulating upload of version ${version} to Nexus repo ${repo}"
        """
    }
}
