package de.westnordost.streetcomplete.util.ktx

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary

/** Get the primary feature of the given [element] or null, in the given [languages] and [country].
 *  [isSuggestion] controls whether brand features are included or not. */
fun FeatureDictionary.getFeature(
    element: Element,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String? = null,
    isSuggestion: Boolean? = false
): Feature? {
    // only if geometry is not a node because at this point we cannot tell apart points vs vertices
    val geometryType = if (element.type == ElementType.NODE) null else element.geometryType

    val features = getByTags(
        tags = element.tags,
        languages = languages,
        country = country,
        geometry = geometryType,
        isSuggestion = isSuggestion
    )

    // see comment above - we want at least only features that can either be nodes or vertices if
    // our element is a node
    return if (element.type == ElementType.NODE) {
        features.firstOrNull { feature ->
            feature.geometry.any { it == GeometryType.POINT || it == GeometryType.VERTEX }
        }
    } else {
        features.firstOrNull()
    }
}

/** Get the primary disused feature of the given [element] or null, in the given [languages] and
 *  [country]. [isSuggestion] controls whether brand features are included or not. */
fun FeatureDictionary.getDisusedFeature(
    disusedString: String,
    element: Element,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String? = null,
    isSuggestion: Boolean? = false
): Feature? {
    val disusedElement = element.asIfItWasnt("disused") ?: return null
    val disusedFeature = getFeature(disusedElement, languages, country, isSuggestion) ?: return null
    return disusedFeature.toPrefixedFeature("disused", disusedString)
}

fun FeatureDictionary.getFeatureById(
    id: String,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String? = null
): Feature? {
    getById(
        id = id,
        languages = languages,
        country = country,
    )
}
