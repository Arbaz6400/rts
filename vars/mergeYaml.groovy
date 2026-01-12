def deepMerge(Object base, Object override) {

    // Map + Map → recursive merge
    if (base instanceof Map && override instanceof Map) {
        Map result = [:]

        base.each { k, v ->
            result[k] = v
        }

        override.each { k, v ->
            if (result[k] != null) {
                result[k] = deepMerge(result[k], v)
            } else {
                result[k] = v
            }
        }
        return result
    }

    // List + List → merge by name
    if (base instanceof List && override instanceof List) {

        List result = []

        // index base list by name
        Map<String, Map> baseIndex = [:]
        base.each { item ->
            if (item instanceof Map && item.name) {
                baseIndex[item.name] = item
            } else {
                result << item
            }
        }

        override.each { item ->
            if (item instanceof Map && item.name && baseIndex[item.name]) {
                result << deepMerge(baseIndex[item.name], item)
                baseIndex.remove(item.name)
            } else {
                result << item
            }
        }

        // add remaining base items
        baseIndex.values().each {
            result << it
        }

        return result
    }

    // Everything else → override wins
    return override
}
