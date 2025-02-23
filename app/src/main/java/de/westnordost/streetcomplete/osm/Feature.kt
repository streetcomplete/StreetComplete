package de.westnordost.streetcomplete.osm

import de.westnordost.osmfeatures.Feature

/** Apply this feature to the given [tags], optionally removing a [previousFeature] first, i.e.
 *  replacing it. */
fun Feature.applyTo(tags: Tags, previousFeature: Feature? = null) {
    if (previousFeature != null) {
        for ((key, value) in previousFeature.removeTags) {
            if (tags[key] == value) tags.remove(key)
        }
        for (key in previousFeature.removeTagKeys) {
            tags.remove(key)
        }
    }
    for ((key, value) in addTagKeys.associateWith { "yes" } + addTags) {
        if (key !in tags || preserveTags.none { it.containsMatchIn(key) }) {
            tags[key] = value
        }
    }
}
