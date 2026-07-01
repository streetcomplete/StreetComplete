package de.westnordost.streetcomplete.osm.places

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.things.isDisusedThing
import de.westnordost.streetcomplete.osm.things.isThing
import de.westnordost.streetcomplete.osm.things.isThingOrDisusedThing
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.osm.toPrefixedFeature
import de.westnordost.streetcomplete.util.ktx.getDisusedFeature
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary

/** Get the primary place feature of the given [element] or null, in the given [languages] and
 *  [country]. [unknownPlaceString] is used to label the place correctly if it is an unknown place
 *  */
fun FeatureDictionary.getPlaceOrDisusedPlace(
    disusedString: String,
    unknownPlaceString: String,
    element: Element,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String?,
): Feature? =
    // either a regular place
    getFeature(element, languages, country, isSuggestion = null) // include brands
        ?.takeIf { it.toElement().isPlace() }
    // or a disused place
    ?: getDisusedFeature(disusedString, element, languages, country)
        ?.takeIf { it.toElement().isPlace() }
    // or vacant place
    ?: (if (element.isDisusedPlace()) getById("shop/vacant", languages, country) else null)
    // or unknown place (i.e. not known by feature dictionary)
    ?: if (element.isPlace()) {
        BaseFeature(
            id = "shop/unknown",
            names = listOf(unknownPlaceString),
            icon = "maki-shop",
            tags = element.tags,
            geometry = GeometryType.entries.toList()
        )
    } else null

/** Get a disused version of the given [element]'s associated feature in the given [languages] and
 * [country].
 * If the kind of place is unknown, returns the generic `shop=vacant` feature */
fun FeatureDictionary.getPlaceAsDisused(
    element: Element,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String?,
): Feature =
    getFeature(element, languages, country, isSuggestion = null)
        ?.takeIf { it.toElement().isPlace() }
        ?.toPrefixedFeature("disused")
    ?: getById("shop/vacant", languages, country)!!
