package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import android.text.Html
import androidx.core.text.parseAsHtml
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.util.ktx.geometryType

fun getNameAndLocationLabel(
    element: Element,
    resources: Resources,
    featureDictionary: FeatureDictionary,
    showHouseNumber: Boolean? = null
): CharSequence? {
    // only if geometry is not a node because at this point we cannot tell apart points vs vertices
    val geometryType = if (element.type == ElementType.NODE) null else element.geometryType
    val feature = featureDictionary.getFeatureName(resources.configuration, element.tags, geometryType)
        ?.withNonBreakingSpaces()
        ?.inItalics()
    val name = getNameLabel(element.tags)
        ?.withNonBreakingSpaces()
        ?.inBold()

    val nameAndFeatureName = if (name != null && feature != null) {
        resources.getString(R.string.label_name_feature, name, feature)
    } else {
        name ?: feature
    }

    // only show house number if there is no name information
    val location = getLocationHtml(element.tags, resources, showHouseNumber =
        if (showHouseNumber == null && nameAndFeatureName != null) false else showHouseNumber
    )

    val label = if (nameAndFeatureName != null && location != null) {
        resources.getString(R.string.label_location_name, location, nameAndFeatureName)
    } else {
        location ?: nameAndFeatureName
    }

    return label?.parseAsHtml()
}

/** Returns a text that describes its location, e.g. "house number 123 - on floor 5" */
fun getLocationLabel(
    tags: Map<String, String>,
    resources: Resources,
    showHouseNumber: Boolean? = null
): CharSequence? =
    getLocationHtml(tags, resources, showHouseNumber)?.parseAsHtml()

private fun getLocationHtml(
    tags: Map<String, String>,
    resources: Resources,
    showHouseNumber: Boolean? = null
): String? {
    val level = getLevelLabel(tags, resources)
    // by default only show house number if no level is given
    val houseNumber = if (showHouseNumber ?: (level == null)) getHouseNumberHtml(tags, resources) else null

    return if (level != null && houseNumber != null) {
        resources.getString(R.string.label_housenumber_location, houseNumber, level)
    } else {
        level ?: houseNumber
    }
}

/** Returns the feature name only, e.g. "Bakery" */
fun FeatureDictionary.getFeatureName(
    configuration: Configuration,
    tags: Map<String, String>,
    geometryType: GeometryType? = null,
): String? = this
    .byTags(tags)
    .isSuggestion(false)
    .forLocale(*getLocalesForFeatureDictionary(configuration))
    .forGeometry(geometryType)
    .find()
    .firstOrNull()
    ?.name

/** Returns a text that identifies the feature by name, ref, brand or whatever, e.g. "The Leaky Cauldron" */
fun getNameLabel(tags: Map<String, String>): String? {
    val name = tags["name"]
    val brand = tags["brand"]
    val localRef = tags["local_ref"]
    val ref = tags["ref"]
    val operator = tags["operator"]

    // Favour local ref over ref as it's likely to be more local/visible, e.g. bus stop point versus text code
    return if (name != null && localRef != null) "$name ($localRef)" else null
        ?: name
        ?: brand
        ?: if (localRef != null && operator != null) "$operator ($localRef)" else null
        ?: if (ref != null && operator != null) "$operator [$ref]" else null
        ?: operator
        ?: localRef
        ?: ref
}

/** Returns a text that describes the floor / level, e.g. "on floor 5" */
fun getLevelLabel(tags: Map<String, String>, resources: Resources): String? {
    /* distinguish between "floor" and "level":
       E.g. addr:floor may be "M" while level is "2". The "2" is in this case purely technical and
       can not be seen on any sign. */
    val floor = tags["addr:floor"] ?: tags["level:ref"]
    if (floor != null) {
        return resources.getString(R.string.on_floor, floor)
    }
    val level = tags["level"]
    if (level != null) {
        return resources.getString(R.string.on_level, level)
    }
    if (tags["tunnel"] == "yes" || tags["tunnel"] == "culvert" || tags["location"] == "underground") {
        return resources.getString(R.string.underground)
    }
    return null
}

/** Returns a text that describes the house number, e.g. "house number 123" */
fun getHouseNumberLabel(tags: Map<String, String>, resources: Resources): CharSequence? =
    getHouseNumberHtml(tags, resources)?.parseAsHtml()

private fun getHouseNumberHtml(tags: Map<String, String>, resources: Resources): String? {
    val houseName = tags["addr:housename"]
    val conscriptionNumber = tags["addr:conscriptionnumber"]
    val streetNumber = tags["addr:streetnumber"]
    val houseNumber = tags["addr:housenumber"]

    if (houseName != null) {
        return resources.getString(R.string.at_housename, houseName.inItalics())
    }
    if (conscriptionNumber != null) {
        val number = if (streetNumber != null) "$conscriptionNumber/$streetNumber" else conscriptionNumber
        return resources.getString(R.string.at_housenumber, number)
    }
    if (houseNumber != null) {
        return resources.getString(R.string.at_housenumber, houseNumber)
    }
    return null
}

/** Returns just the house number as it would be signed if set, e.g. "123" */
fun getShortHouseNumber(map: Map<String, String>): String? {
    val houseName = map["addr:housename"]
    val conscriptionNumber = map["addr:conscriptionnumber"]
    val streetNumber = map["addr:streetnumber"]
    val houseNumber = map["addr:housenumber"]

    return when {
        houseName != null -> houseName
        conscriptionNumber != null && streetNumber != null -> "$conscriptionNumber / $streetNumber"
        conscriptionNumber != null -> conscriptionNumber
        houseNumber != null -> houseNumber
        else -> null
    }
}

private fun String.inBold(): String = "<b>${Html.escapeHtml(this)}</b>"
private fun String.inItalics(): String = "<i>${Html.escapeHtml(this)}</i>"

private fun String.withNonBreakingSpaces(): String = replace(' ', ' ')
