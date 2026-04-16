package de.westnordost.streetcomplete.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.Locale.Companion
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.util.formatAnnotated
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getSystemResourceEnvironment

@Composable
fun nameAndLocationLabel(
    element: Element,
    featureDictionary: FeatureDictionary?,
    countryCode: String?,
    showHouseNumber: Boolean? = null
): AnnotatedString? {
    val locale = Locale.current
    val theme = isSystemInDarkTheme()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val resourceEnvironment = remember(locale, theme, density) { getSystemResourceEnvironment() }
    val textState by produceState<AnnotatedString?>(initialValue = null) {
        value = getNameAndLocationLabel(
            resourceEnvironment = resourceEnvironment,
            layoutDirection = layoutDirection,
            countryCode = countryCode,
            element = element,
            featureDictionary = featureDictionary,
            showHouseNumber = showHouseNumber
        )
    }
    return textState
}

/** Returns a text that helps to locate an element. It may look like this...
 *
 *  - house number 12, on level 2: *Deciduous tree - Taxus cuspidata*
 *  - house name *OpenStreetMap Center*, outside: **Commerzbank** (*ATM*)
 *
 *  or any subset of that.
 *
 *  When [featureDictionary] is null, the feature name will not be included (same as if no feature
 *  name is found). When [showHouseNumber] is null, the house number is only shown if the element
 *  doesn't have a name, and it's not located in some mall or something (has a `level` tag).
 *  */
suspend fun getNameAndLocationLabel(
    resourceEnvironment: ResourceEnvironment,
    layoutDirection: LayoutDirection,
    countryCode: String?,
    element: Element,
    featureDictionary: FeatureDictionary?,
    showHouseNumber: Boolean? = null
): AnnotatedString? {
    val feature = featureDictionary
        ?.getFeature(element, getLanguagesForFeatureDictionary())
        ?.name
        ?.withNonBreakingSpaces()
        ?.inItalics()

    val name = getNameLabel(element.tags)
        ?.withNonBreakingSpaces()
        ?.inBold()

    val taxon = getTreeTaxon(element.tags, Locale.current.language)

    val featureEx = if (taxon != null && feature != null) {
        when (layoutDirection) {
            LayoutDirection.Ltr -> annotatedStringOf(feature, " - ", taxon)
            LayoutDirection.Rtl -> annotatedStringOf(taxon, " - ", feature)
        }
    } else {
        feature ?: taxon?.let { AnnotatedString(it) }
    }

    val nameAndFeatureName = if (name != null && featureEx != null) {
        when (layoutDirection) {
            LayoutDirection.Ltr -> annotatedStringOf(name, " (", featureEx, ")")
            LayoutDirection.Rtl -> annotatedStringOf("(", featureEx, ") ", name)
        }
    } else {
        name ?: featureEx
    }

    // only show house number if there is no name
    val location = getLocationLabel(resourceEnvironment, layoutDirection, countryCode, element.tags,
        showHouseNumber = if (showHouseNumber == null && name != null) false else showHouseNumber
    )

    val label = if (nameAndFeatureName != null && location != null) {
        when (layoutDirection) {
            LayoutDirection.Ltr -> annotatedStringOf(location, ": ", nameAndFeatureName)
            LayoutDirection.Rtl -> annotatedStringOf(nameAndFeatureName, " :", location)
        }
    } else {
        location ?: nameAndFeatureName
    }

    return label
}

/** Returns a text that describes its location, e.g. "house number 123 - on floor 5" */
suspend fun getLocationLabel(
    resourceEnvironment: ResourceEnvironment,
    layoutDirection: LayoutDirection,
    countryCode: String?,
    tags: Map<String, String>,
    showHouseNumber: Boolean? = null,
): AnnotatedString? {
    val level = getLevelLabel(resourceEnvironment, tags)
    // by default only show house number if no level is given
    val houseNumber =
        if (showHouseNumber ?: (level == null)) getHouseNumberLabel(resourceEnvironment, countryCode, tags)
        else null
    val location = level ?: getIndoorOutdoorLabel(resourceEnvironment, tags)

    return if (location != null && houseNumber != null) {
        when (layoutDirection) {
            LayoutDirection.Ltr -> annotatedStringOf(houseNumber, " - ", location)
            LayoutDirection.Rtl -> annotatedStringOf(location, " - ", houseNumber)
        }
    } else {
        location?.let { AnnotatedString(it) } ?: houseNumber
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

    // Favor local ref over ref as it's likely to be more local/visible, e.g. bus stop point versus text code
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
suspend fun getIndoorOutdoorLabel(
    resourceEnvironment: ResourceEnvironment,
    tags: Map<String, String>
): String? = when {
    tags["indoor"] == "yes" || tags["location"] == "indoor" -> {
        getString(resourceEnvironment, Res.string.inside)
    }
    tags["indoor"] == "no" || tags["location"] == "outdoor" -> {
        getString(resourceEnvironment, Res.string.outside)
    }
    else -> {
        null
    }
}

/** Returns a text that describes the floor / level, e.g. "on floor 5" */
suspend fun getLevelLabel(
    resourceEnvironment: ResourceEnvironment,
    tags: Map<String, String>
): String? {
    /* distinguish between "floor" and "level":
       E.g. addr:floor may be "M" while level is "2". The "2" is in this case purely technical and
       can not be seen on any sign. */
    val floor = tags["addr:floor"] ?: tags["level:ref"]
    if (floor != null) {
        return getString(resourceEnvironment, Res.string.on_floor, floor)
    }
    val level = tags["level"]
    if (level != null) {
        return getString(resourceEnvironment, Res.string.on_level, level)
    }
    if (tags["tunnel"] == "yes" || tags["tunnel"] == "culvert" || tags["location"] == "underground") {
        return getString(resourceEnvironment, Res.string.underground)
    }
    val bridge = tags["bridge"]
    if (bridge != null && bridge != "no") {
        return getString(resourceEnvironment, Res.string.bridge)
    }
    return null
}

/** Returns a text that describes the house number, e.g. "house number 123" */
private suspend fun getHouseNumberLabel(
    resourceEnvironment: ResourceEnvironment,
    countryCode: String?,
    tags: Map<String, String>
): AnnotatedString? {
    val houseName = tags["addr:housename"]
    val conscriptionNumber = tags["addr:conscriptionnumber"]
    val streetNumber = tags["addr:streetnumber"]
    val houseNumber = tags["addr:housenumber"]
    val subHouseNumber = tags["addr:unit"] ?: tags["addr:flats"]

    if (houseName != null) {
        return getString(resourceEnvironment, Res.string.at_housename)
            .formatAnnotated(houseName.inItalics())
    }
    if (conscriptionNumber != null) {
        val number = if (streetNumber != null) "$conscriptionNumber/$streetNumber" else conscriptionNumber
        return getString(resourceEnvironment, Res.string.at_housenumber)
            .formatAnnotated(number)
    }
    if (houseNumber != null) {
        val houseNumberEx = if (subHouseNumber != null) {
            if (countryCode in listOf("AU", "NZ")) {
                "$subHouseNumber $houseNumber"
            } else {
                "$houseNumber $subHouseNumber"
            }
        } else {
            houseNumber
        }
        return getString(resourceEnvironment, Res.string.at_housenumber)
            .formatAnnotated(houseNumberEx)
    }
    return null
}

/** Returns just the house number as it would be signed, e.g. "123", if set */
fun getShortHouseNumber(tags: Map<String, String>, countryCode: String?): String? {
    val houseName = tags["addr:housename"]
    val conscriptionNumber = tags["addr:conscriptionnumber"]
    val streetNumber = tags["addr:streetnumber"]
    val houseNumber = tags["addr:housenumber"]
    val subHouseNumber = tags["addr:unit"] ?: tags["addr:flats"]

    return when {
        houseName != null -> houseName
        conscriptionNumber != null && streetNumber != null -> "$conscriptionNumber / $streetNumber"
        conscriptionNumber != null -> conscriptionNumber
        houseNumber != null && subHouseNumber != null -> {
            if (countryCode in listOf("AU", "NZ")) {
                "$subHouseNumber $houseNumber"
            } else {
                "$houseNumber $subHouseNumber"
            }
        }
        houseNumber != null -> houseNumber
        else -> null
    }
}

private fun String.withNonBreakingSpaces(): String = replace(' ', '\u00A0')

private fun CharSequence.inItalics(): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            append(this@inItalics)
        }
    }

private fun CharSequence.inBold(): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(this@inBold)
        }
    }

private fun annotatedStringOf(vararg strings: CharSequence?) : AnnotatedString =
    buildAnnotatedString {
        for (string in strings) {
            append(string)
        }
    }
