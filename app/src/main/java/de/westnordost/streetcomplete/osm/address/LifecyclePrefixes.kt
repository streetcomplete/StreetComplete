package de.westnordost.streetcomplete.osm.address

import android.content.Context
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.overlays.street_furniture.DummyFeature

fun featureBehindPrefix(tags: Map<String, String>, prefix: String, featureDictionary: FeatureDictionary): Feature? {
    val prefixedTags = tags.filter { it.key.startsWith(prefix) }
    return featureDictionary
        .byTags(prefixedTags)
        .find()
        .firstOrNull()
}

fun featureBehindPrefixUsingGetFeatureFunction(tags: Map<String, String>, prefix: String, getFeature: (tags: Map<String, String>) -> Feature?): Feature? {
    val prefixedTags = tags.filter { it.key.startsWith(prefix) }
    return getFeature(prefixedTags)
}

fun reconstructDisusedFeatureFromProcessedTags(context: Context, rawTags: Map<String, String>, feature: Feature?): DummyFeature {
    if (feature != null) {
        val name = context.getString(R.string.unspecified_disused_object) + "\n" + feature.name
        return DummyFeature(
            "reconstructed_from_lifecycle_prefix/known_disused",
            name,
            "ic_preset_maki_marker_stroked",
            rawTags
        )
    } else {
        return DummyFeature(
            "reconstructed_from_lifecycle_prefix/unknown_disused",
            context.getString(R.string.unspecified_disused_object),
            "ic_preset_maki_marker_stroked",
            rawTags
        )
    }
}

fun reconstructFeature(context: Context, tags: Map<String, String>, prefix: String, featureDictionary: FeatureDictionary): DummyFeature {
    val prefixed = featureBehindPrefix(tags, prefix, featureDictionary)
    return reconstructDisusedFeatureFromProcessedTags(context, tags, prefixed)
}

fun reconstructFeatureUsingGetFeatureFunction(context: Context, tags: Map<String, String>, prefix: String, getFeature: (tags: Map<String, String>) -> Feature?): DummyFeature {
    val prefixed = featureBehindPrefixUsingGetFeatureFunction(tags, prefix, getFeature)
    return reconstructDisusedFeatureFromProcessedTags(context, tags, prefixed)
}
