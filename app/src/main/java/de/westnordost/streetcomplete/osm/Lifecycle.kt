package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.util.ktx.copy

/** Returns a copy of this element with all tags prefixed with the given [lifecycle] prefix moved
 *  to normal tags space and all others cleared.
 *  Returns `null` if the element has no tags with the given [lifecycle] prefix
 *
 *  E.g. when calling this method with [lifecycle] = "disused" for an element with the tags
 *  `building=yes` + `disused:shop=yes` , a copy of that element is returned with the only tag
 *  `shop=yes`. */
fun Element.asIfItWasnt(lifecycle: String): Element? =
    if (tags.hasPrefixed(lifecycle)) copy(tags = tags.getPrefixedOnly(lifecycle)) else null

private fun Map<String, String>.hasPrefixed(prefix: String): Boolean =
    any { it.key.startsWith("$prefix:") }

private fun Map<String, String>.getPrefixedOnly(prefix: String): Map<String, String> = this
    .filter { it.key.startsWith("$prefix:") }
    .mapKeys { it.key.substring(prefix.length + 1) }
