package de.westnordost.streetcomplete.util.ktx

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.util.Locale

fun FeatureDictionary.getFeature(
    element: Element,
    locales: Array<Locale?>? = null,
): Feature? {
    // only if geometry is not a node because at this point we cannot tell apart points vs vertices
    val geometryType = if (element.type == ElementType.NODE) null else element.geometryType
    val builder = this
        .byTags(element.tags)
        .isSuggestion(false) // no brands
        .forGeometry(geometryType)
    if (locales != null) builder.forLocale(*locales)
    val features = builder.find()

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
