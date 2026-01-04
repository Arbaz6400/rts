package org.enbd.rts

class SecretLoader implements Serializable {

    static Map load(def steps, String secretsFilePath) {

        Map secretsMap = [:]

        if (!steps.fileExists(secretsFilePath)) {
            steps.echo "Secrets file not found: ${secretsFilePath}"
            return secretsMap
        }

        def yaml = steps.readYaml(file: secretsFilePath)

        if (!yaml?.secrets) {
            steps.echo "Secrets file present but empty"
            return secretsMap
        }

        yaml.secrets.each { secret ->

            String vaultPath = secret.path
            String key       = secret.key
            String argName   = secret.extraProgramArgName

            String envVar = argName
                    .toUpperCase()
                    .replace('.', '_')

            steps.withCredentials([
                steps.string(
                    credentialsId: "${vaultPath}:${key}",
                    variable: envVar
                )
            ]) {
                secretsMap[argName] = steps.env[envVar]
            }
        }

        return secretsMap
    }
}
