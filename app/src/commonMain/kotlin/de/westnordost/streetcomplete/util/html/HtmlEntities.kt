package de.westnordost.streetcomplete.util.html

/** Replaces e.g. `&lt;` with `<` etc. */
fun String.unescapeHtmlEntities(): String =
    replace(ENTITY_REGEX) { ENTITIES[it.value]?.toString() ?: it.value }

/** Replaces e.g. `<` with `&lt;` etc. */
fun String.escapeHtmlEntities(): String {
    val result = StringBuilder()
    for (c in this) {
        val entity = TO_ENTITIES[c]
        if (entity != null) result.append(entity) else result.append(c)
    }
    return result.toString()
}


private val ENTITY_REGEX by lazy { Regex("&[a-zA-Z0-9]+;") }

private val ENTITIES: Map<String, Char> = mapOf(
    "&quot;" to '"',
    "&amp;" to '&',
    "&lt;" to '<',
    "&gt;" to '>',
)

private val TO_ENTITIES: Map<Char, String> =
    ENTITIES.entries.associate { (key, value) -> value to key }
