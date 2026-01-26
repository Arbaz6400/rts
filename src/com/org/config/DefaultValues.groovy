package com.org.config

class DefaultValues {

    static Map defaults(String branch) {

        branch = branch?.toLowerCase()

        switch(branch) {

            case "uat":
                return uat()

            case "pre-prod":
            case "preprod":
                return preProd()

            case "main":
            case "prod":
                return prod()

            default:
                // safe fallback
                return uat()
        }
    }

    private static Map uat() {
        return [
            namespace : "uat-namespace",
            vaultPath : "secret/data/uat/app",
            vaultToken: "uat-token",
            env       : "uat"
        ]
    }

    private static Map preProd() {
        return [
            namespace : "preprod-namespace",
            vaultPath : "secret/data/preprod/app",
            vaultToken: "preprod-token",
            env       : "pre-prod"
        ]
    }

    private static Map prod() {
        return [
            namespace : "prod-namespace",
            vaultPath : "secret/data/prod/app",
            vaultToken: "prod-token",
            env       : "prod"
        ]
    }
}
