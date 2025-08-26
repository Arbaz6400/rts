// vars/nexusUpload.groovy
def call(String version, String repo) {
    // Create instance of your Nexus helper
    def nexus = new org.enbd.common.NexusRest(this)
    nexus.uploadReleaseProdNexus(version, repo)
}
