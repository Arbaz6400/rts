def call(String version, String repo) {
    def nexus = new org.enbd.common.NexusRest(this)
    nexus.uploadReleaseProdNexus(version, repo)
}
