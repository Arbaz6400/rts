package com.org.config

class DefaultValues {

    static Map defaults() {
        return [
            namespace : "default",
            vaultPath : "secret/data/app",
            vaultToken: "changeme",
            env       : "uat"
        ]
    }
}
