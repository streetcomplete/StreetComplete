package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.iconDrawableResource
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.getShortHouseNumber
import de.westnordost.streetcomplete.util.ktx.getFeature
import org.jetbrains.compose.resources.DrawableResource

fun getIcon(featureDictionary: FeatureDictionary, element: Element): DrawableResource? {
    val icon = featureDictionary.getFeature(element)?.iconDrawableResource
    if (icon != null) return icon

    if (getShortHouseNumber(element.tags) != null && getNameLabel(element.tags) == null) {
        return Res.drawable.none
    }

    return null
}

fun getTitle(tags: Map<String, String>): String? =
    getNameLabel(tags) ?: getShortHouseNumber(tags)
