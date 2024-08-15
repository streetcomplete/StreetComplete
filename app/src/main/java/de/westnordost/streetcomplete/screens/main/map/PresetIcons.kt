package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.getShortHouseNumber
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.view.presetIconIndex

@DrawableRes fun getIcon(featureDictionary: FeatureDictionary, element: Element): Int? {
    val icon = featureDictionary.getFeature(element)?.let { presetIconIndex[it.icon] }
    if (icon != null) return icon

    if (getShortHouseNumber(element.tags) != null && getNameLabel(element.tags) == null) {
        return R.drawable.ic_none
    }

    return null
}

fun getTitle(tags: Map<String, String>, languages: Collection<String> = emptyList()): String? {
    return getNameLabel(tags) ?: getShortHouseNumber(tags) ?: getTreeGenus(tags, languages)
}

// prefer tree species in provided languages, then osm tag, then other languages
fun getTreeGenus(tags: Map<String, String>, languages: Collection<String> = emptyList()): String? {
    if (tags["natural"] != "tree") return null
    languages.forEach { lc ->
        tags["species:$lc"]?.let { return it }
        tags["genus:$lc"]?.let { return it }
    }
    tags["species"]?.let { return it }
    tags["genus"]?.let { return it }
    tags.forEach { (key, value) ->
        if (key.startsWith("genus:") || key.startsWith("species:"))
            return value
    }
    return null
}
