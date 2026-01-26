package com.org.config

class DefaultValues implements Serializable {

    static Map defaults() {
        return [
            namespace : "default",
            vaultPath : "secret/data/app",
            vaultToken: "changeme",
            env       : "dev"
        ]
    }
}
