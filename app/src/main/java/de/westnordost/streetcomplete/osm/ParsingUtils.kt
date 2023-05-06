package de.westnordost.streetcomplete.osm

/** Expands [prefix]:both:[suffix] and [prefix]:[suffix] (if [useNakedTag] is `true`) into
 *  the keys [prefix]:left:[suffix] and [prefix]:right:[suffix] if they don't exist in the map yet.
 *  */
fun MutableMap<String, String>.expandSidesTags(
    prefix: String,
    suffix: String,
    useNakedTag: Boolean
) {
    val pre = prefix
    val post = if (suffix.isEmpty()) "" else ":$suffix"
    val value = get("$pre:both$post") ?: if (useNakedTag) get("$pre$post") else null
    if (value != null) {
        if (!containsKey("$pre:left$post")) set("$pre:left$post", value)
        if (!containsKey("$pre:right$post")) set("$pre:right$post", value)
    }
}
