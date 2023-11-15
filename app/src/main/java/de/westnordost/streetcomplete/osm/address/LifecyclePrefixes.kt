package de.westnordost.streetcomplete.osm.address

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary

fun featureBehindPrefix(tags: Map<String, String>, prefix: String, featureDictionary: FeatureDictionary): Feature? {
    val prefixedTags = tags.filter { it.key.startsWith(prefix) }
    return featureDictionary
        .byTags(prefixedTags)
        .find()
        .firstOrNull()
}
