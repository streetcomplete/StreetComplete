package de.westnordost.streetcomplete.osm.things

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.toElement
import de.westnordost.streetcomplete.util.ktx.getDisusedFeature
import de.westnordost.streetcomplete.util.ktx.getFeature
import de.westnordost.streetcomplete.util.locale.getLanguagesForFeatureDictionary

/** Get the primary thing feature of the given [element] or null, in the given [languages] and
 *  [country]. [disusedString] and [unknownThingString] are used to label the thing correctly
 *  if it is a disused thing or unknown thing, respectively. */
fun FeatureDictionary.getThingOrDisusedThing(
    disusedString: String,
    unknownThingString: String,
    element: Element,
    languages: List<String?>? = getLanguagesForFeatureDictionary(),
    country: String?,
): Feature? =
    // either a thing
    getFeature(element, languages, country)
        ?.takeIf { it.toElement().isThing() }
    // or a disused thing
    ?: getDisusedFeature(disusedString, element, languages, country)
        ?.takeIf { it.toElement().isDisusedThing() }
    // or unknown thing (i.e. not known by feature dictionary)
    ?: if (element.isThingOrDisusedThing()) {
        BaseFeature(
            id = "thing/unknown",
            names = listOf(unknownThingString),
            icon = "preset_maki_marker_stroked",
            tags = element.tags,
            geometry = GeometryType.entries.toList()
        )
    } else null
