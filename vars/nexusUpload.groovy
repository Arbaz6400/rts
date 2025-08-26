def call(String pomLocation, String repository, Boolean shadowJar_plugin = true, String version = null) {
    def nexus = new org.enbd.common.NexusRest(this)
    nexus.uploadReleaseProdNexus(pomLocation, repository, shadowJar_plugin, version)
}
