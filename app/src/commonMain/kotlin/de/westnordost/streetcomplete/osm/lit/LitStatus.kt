package de.westnordost.streetcomplete.osm.lit

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.UNSUPPORTED
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

enum class LitStatus {
    YES,
    NO,
    AUTOMATIC,
    NIGHT_AND_DAY,
    UNSUPPORTED
}

/** Returns the lit status as an enum */
fun parseLitStatus(tags: Map<String, String>): LitStatus? = when (tags["lit"]) {
    "yes" -> YES
    "no" -> NO
    "automatic" -> AUTOMATIC
    "24/7" -> NIGHT_AND_DAY
    // invalid ones count as if not set
    null, "lit", "unlit", "Unlit", "No", "unknown", "sunset-00:00,05:00-sunrise UTC" -> null
    /* unsupported values that are not selectable in the app but should not be overwritten with
       "yes" as long as there is no viable replacement in order to not destroy data */
    "interval", "limited", "sunset-sunrise", "dusk-dawn" -> UNSUPPORTED
    else -> UNSUPPORTED
}

fun LitStatus.applyTo(tags: Tags) {
    val litValue = when (this) {
        YES -> {
            if (parseLitStatus(tags) == UNSUPPORTED) {
                tags.updateCheckDateForKey("lit")
                return
            } else {
                "yes"
            }
        }
        NO -> "no"
        AUTOMATIC -> "automatic"
        NIGHT_AND_DAY -> "24/7"
        UNSUPPORTED -> throw IllegalArgumentException()
    }
    tags.updateWithCheckDate("lit", litValue)
}
