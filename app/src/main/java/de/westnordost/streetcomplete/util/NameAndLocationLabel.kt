package de.westnordost.streetcomplete.util

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.text.parseAsHtml
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.util.ktx.getFeature
import java.util.Locale

fun getNameAndLocationSpanned(
    element: Element,
    resources: Resources,
    featureDictionary: FeatureDictionary?,
    showHouseNumber: Boolean? = null
): Spanned? =
    getNameAndLocationHtml(element, resources, featureDictionary, showHouseNumber)?.parseAsHtml()

fun getNameAndLocationHtml(
    element: Element,
    resources: Resources,
    featureDictionary: FeatureDictionary?,
    showHouseNumber: Boolean? = null
): String? {
    val languages = getLanguagesForFeatureDictionary(resources.configuration)
    val feature = featureDictionary
        ?.getFeature(element, languages)
        ?.name
        ?.withNonBreakingSpaces()
        ?.inItalics()
    val name = getNameLabel(element.tags)
        ?.withNonBreakingSpaces()
        ?.inBold()
    val taxon = getTreeTaxon(element.tags, Locale.getDefault().language)

    val featureEx = if (taxon != null && feature != null) {
        resources.getString(R.string.label_feature_taxon, feature, taxon)
    } else {
        feature ?: taxon
    }

    val nameAndFeatureName = if (name != null && featureEx != null) {
        resources.getString(R.string.label_name_feature, name, featureEx)
    } else {
        name ?: featureEx
    }

    // only show house number if there is no name
    val location = getLocationHtml(element.tags, resources, showHouseNumber =
        if (showHouseNumber == null && name != null) false else showHouseNumber
    )

    val label = if (nameAndFeatureName != null && location != null) {
        resources.getString(R.string.label_location_name, location, nameAndFeatureName)
    } else {
        location ?: nameAndFeatureName
    }

    return label
}

/** Returns a text that describes its location, e.g. "house number 123 - on floor 5" */
fun getLocationSpanned(
    tags: Map<String, String>,
    resources: Resources,
    showHouseNumber: Boolean? = null
): Spanned? =
    getLocationHtml(tags, resources, showHouseNumber)?.parseAsHtml()

fun getLocationHtml(
    tags: Map<String, String>,
    resources: Resources,
    showHouseNumber: Boolean? = null
): String? {
    val level = getLevelLabel(tags, resources)
    // by default only show house number if no level is given
    val houseNumber = if (showHouseNumber ?: (level == null)) getHouseNumberHtml(tags, resources) else null
    val indoor = getIndoorOutdoorLabel(tags, resources)
    val location = level ?: indoor

    return if (location != null && houseNumber != null) {
        resources.getString(R.string.label_housenumber_location, houseNumber, location)
    } else {
        location ?: houseNumber
    }
}

/** Returns the taxon of a tree or null if unknown */
fun getTreeTaxon(tags: Map<String, String>, languageTag: String): String? {
    if (tags["natural"] != "tree") return null

    val names = sequenceOf("taxon", "species", "taxon:species", "genus", "taxon:genus")

    return names.firstNotNullOfOrNull { tags["$it:$languageTag"] }
        ?: names.firstNotNullOfOrNull { tags[it] }
}

/** Returns a text that identifies the feature by name, ref, brand or whatever, e.g. "The Leaky Cauldron" */
fun getNameLabel(tags: Map<String, String>): String? {
    val name = tags["name"]
    val brand = tags["brand"]
    val localRef = tags["local_ref"]
    val ref = tags["ref"]
    val operator = tags["operator"]

    if (tags["highway"] in ALL_ROADS) {
        val nameAndLocalRef = if (name != null && localRef != null) "$name [$localRef]" else null
        val nameAndRef = if (name != null && ref != null) "$name [$ref]" else null

        return nameAndLocalRef
            ?: nameAndRef
            ?: name
            ?: localRef
            ?: ref
    }

    val nameAndLocalRef = if (name != null && localRef != null) "$name ($localRef)" else null
    val operatorAndLocalRef = if (localRef != null && operator != null) "$operator ($localRef)" else null
    val operatorAndRef = if (ref != null && operator != null) "$operator [$ref]" else null

    // Favour local ref over ref as it's likely to be more local/visible, e.g. bus stop point versus text code
    return nameAndLocalRef
        ?: name
        ?: brand
        ?: operatorAndLocalRef
        ?: operatorAndRef
        ?: operator
        ?: localRef
        ?: ref
}

/** Returns a text that describes whether it is inside or outside (of a building) */
fun getIndoorOutdoorLabel(tags: Map<String, String>, resources: Resources): String? = when {
    tags["indoor"] == "yes" || tags["location"] == "indoor" -> resources.getString(R.string.inside)
    tags["indoor"] == "no" || tags["location"] == "outdoor" -> resources.getString(R.string.outside)
    else -> null
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
    val bridge = tags["bridge"]
    if (bridge != null && bridge != "no") {
        return resources.getString(R.string.bridge)
    }
    return null
}

/** Returns a text that describes the house number, e.g. "house number 123" */
fun getHouseNumberSpanned(tags: Map<String, String>, resources: Resources): Spanned? =
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
