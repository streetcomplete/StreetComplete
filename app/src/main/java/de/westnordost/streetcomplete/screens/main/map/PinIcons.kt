package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.getShortHouseNumber
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.view.presetIconIndex

@DrawableRes fun getPinIcon(featureDictionary: FeatureDictionary, element: Element): Int? {
    val icon = featureDictionary.getFeature(element)?.let { presetIconIndex[it.icon] }
    if (icon != null) return icon

    if (getShortHouseNumber(element.tags) != null && getNameLabel(element.tags) == null) {
        return R.drawable.ic_none
    }

    return null
}

fun getTitle(tags: Map<String, String>): String? =
    getNameLabel(tags) ?: getShortHouseNumber(tags)
