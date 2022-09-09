package de.westnordost.streetcomplete.util

import android.content.res.Resources
import android.text.Html
import android.text.Spanned
import androidx.core.os.ConfigurationCompat
import androidx.core.text.parseAsHtml
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.toList
import java.util.Locale

fun getNameAndLocationLabelString(
    tags: Map<String, String>,
    resources: Resources,
    featureDictionary: FeatureDictionary,
    alwaysShowHouseNumber: Boolean = false
): Spanned? {
    val localeList = ConfigurationCompat.getLocales(resources.configuration).toList()
    val feature = getFeatureName(tags, featureDictionary, localeList)
        ?.withNonBreakingSpaces()?.inItalics()
    val name = getNameLabel(tags)?.withNonBreakingSpaces()?.inBold()
    val level = getLevelLabel(tags, resources)
    val houseNumber = getHouseNumberLabel(tags, resources)
    // only show house number if there is neither name nor level information
    val location = level ?: if (name == null || alwaysShowHouseNumber) houseNumber else null

    return if (location != null) {
        val showHouseNumber = alwaysShowHouseNumber && (houseNumber != null && houseNumber != location)
        if (name != null && feature != null) {
            if (showHouseNumber) {
                resources.getString(R.string.label_housenumber_location_name_feature, houseNumber, location, name, feature)
            } else {
                resources.getString(R.string.label_location_name_feature, location, name, feature)
            }
        } else if (name != null || feature != null) {
            if (showHouseNumber) {
                resources.getString(R.string.label_housenumber_location_name, houseNumber, location, name ?: feature)
            } else {
                resources.getString(R.string.label_location_name, location, name ?: feature)
            }
        } else {
            location
        }
    } else {
        if (name != null && feature != null) {
            resources.getString(R.string.label_name_feature, name, feature)
        } else {
            name ?: feature
        }
    }?.parseAsHtml()
}

private fun getFeatureName(
    tags: Map<String, String>,
    featureDictionary: FeatureDictionary,
    localeList: List<Locale>
): String? {
    val locales = localeList.toMutableList()
    /* add fallback to English if (some) English is not part of the locale list already as the
       fallback for text is also always English in this app (strings.xml) independent of, or rather
       additionally to what is in the user's LocaleList. */
    if (locales.none { it.language == Locale.ENGLISH.language }) {
        locales.add(Locale.ENGLISH)
    }
    return featureDictionary
        .byTags(tags)
        // not for geometry because at this point we cannot tell apart points and vertices
        // .forGeometry(element?.geometryType)
        .isSuggestion(false)
        .forLocale(*locales.toTypedArray())
        .find()
        .firstOrNull()
        ?.name
}

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

fun getHouseNumberLabel(tags: Map<String, String>, resources: Resources): String? {
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

private fun String.inBold(): String = "<b>${Html.escapeHtml(this)}</b>"
private fun String.inItalics(): String = "<i>${Html.escapeHtml(this)}</i>"

private fun String.withNonBreakingSpaces(): String = replace(' ', ' ')
