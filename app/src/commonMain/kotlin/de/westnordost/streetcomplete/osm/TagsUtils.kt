package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

typealias Tags = StringMapChangesBuilder

/**
 * Expands `:both` tags to `:left` and `:right` tags.
 *
 * For example, if [key] is `"sidewalk"`, `sidewalk:both=X` is replaced with `sidewalk:left=X` and
 * `sidewalk:right=X`.
 *
 * If [includeBareTag] is `true`, also in this case `sidewalk=X` is replaced
 * with `sidewalk:left=X` and `sidewalk:right=X`.
 *
 * [postfix] is appended to the key name.
 */
fun Tags.expandSides(key: String, postfix: String? = null, includeBareTag: Boolean = true) {
    val post = if (postfix != null) ":$postfix" else ""
    val both = get("$key:both$post") ?: (if (includeBareTag) get("$key$post") else null)
    if (both != null) {
        // *:left/right is seen as more specific/correct in case the two contradict each other
        if (!containsKey("$key:left$post")) set("$key:left$post", both)
        if (!containsKey("$key:right$post")) set("$key:right$post", both)
    }
    remove("$key:both$post")
    if (includeBareTag) remove("$key$post")
}

/**
 * Replaces `:left` and `:right` tags that are identical with `:both` tags.
 *
 * For example, if [key] is `"sidewalk"`, `sidewalk:left=X` and `sidewalk:right=X` is replaced with
 * `sidewalk:both=X`.
 *
 * [postfix] is appended to the key name.
 */
fun Tags.mergeSides(key: String, postfix: String? = null) {
    val post = if (postfix != null) ":$postfix" else ""
    val left = get("$key:left$post")
    val right = get("$key:right$post")
    if (left != null && left == right) {
        set("$key:both$post", left)
        remove("$key:left$post")
        remove("$key:right$post")
    }
}
