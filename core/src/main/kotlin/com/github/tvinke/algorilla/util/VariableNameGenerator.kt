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
        val base = stripCollectionSuffix(extractSimpleName(collectionName))
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
        val base = stripCollectionSuffix(extractSimpleName(collectionName))
        val keyCapitalized =
            keyName
                .replaceFirstChar { it.uppercaseChar() }
        return "${base}By$keyCapitalized"
    }

    /**
     * Extracts a simple variable name from an expression.
     * "product.getImages()" → "images", "isoCodes" → "isoCodes",
     * "attribute.getProductOption().getDescriptions()" → "descriptions"
     */
    internal fun extractSimpleName(expression: String): String {
        // Take last segment of a dotted chain
        val lastSegment = expression.substringAfterLast('.')

        // Strip trailing ()
        val withoutParens = lastSegment.removeSuffix("()")

        // Strip get prefix: getImages → images, getDescriptions → descriptions
        val withoutGet =
            if (withoutParens.length > GET_PREFIX_LENGTH &&
                withoutParens.startsWith("get") &&
                withoutParens[GET_PREFIX_LENGTH].isUpperCase()
            ) {
                withoutParens.removePrefix("get").replaceFirstChar { it.lowercaseChar() }
            } else {
                withoutParens
            }

        return withoutGet.ifEmpty { expression }
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

    private const val GET_PREFIX_LENGTH = 3
}
