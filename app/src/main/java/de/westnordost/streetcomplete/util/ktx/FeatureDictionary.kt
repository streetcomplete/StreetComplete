package de.westnordost.streetcomplete.util.ktx

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary

fun FeatureDictionary.getFeature(tags: Map<String, String>): Feature? =
    this.byTags(tags)
        .isSuggestion(false) // no brands
        .find().firstOrNull()
