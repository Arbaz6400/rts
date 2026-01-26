
package com.company.config

class DefaultValues {

    /**
     * Central place for all default values.
     * These will be applied ONLY when missing in values.yaml
     */
    static Map get() {

        return [

            namespace : "default",

            env : "dev",

            vault : [
                path  : "secret/data/app",
                token : ""   // intentionally empty – user/CI should override
            ]
        ]
    }

    /**
     * Optional validation for required fields
     */
    static void validate(Map userValues) {

        if (!userValues?.vault?.token) {
            throw new Exception("vault.token must be provided (cannot use default)")
        }
    }
}
