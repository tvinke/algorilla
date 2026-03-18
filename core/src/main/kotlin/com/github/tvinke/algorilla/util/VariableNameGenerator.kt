package com.github.tvinke.algorilla.util

/**
 * Generates idiomatic variable names for pre-built data structures.
 */
public object VariableNameGenerator {
    /**
     * Suggests a Set variable name from a collection variable name.
     * e.g. "orders" → "orderSet", "itemList" → "itemSet"
     */
    public fun suggestSetName(collectionName: String): String {
        val base = stripCollectionSuffix(collectionName)
        return "${base}Set"
    }

    /**
     * Suggests a Map variable name from a collection and key.
     * e.g. ("orders", "id") → "ordersById", ("users", "email") → "usersByEmail"
     */
    public fun suggestMapName(
        collectionName: String,
        keyName: String,
    ): String {
        val base = stripCollectionSuffix(collectionName)
        val keyCapitalized =
            keyName
                .replaceFirstChar { it.uppercaseChar() }
        return "${base}By$keyCapitalized"
    }

    private fun stripCollectionSuffix(name: String): String {
        val suffixes = listOf("List", "Collection", "Array", "Set", "Items", "Elements")
        for (suffix in suffixes) {
            if (name.endsWith(suffix) && name.length > suffix.length) {
                return name.removeSuffix(suffix)
            }
        }
        return name
    }
}
