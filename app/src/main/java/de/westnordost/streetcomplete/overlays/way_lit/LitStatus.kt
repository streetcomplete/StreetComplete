package de.westnordost.streetcomplete.overlays.way_lit

import de.westnordost.streetcomplete.data.osm.mapdata.Element

enum class LitStatus {
    YES,
    NO,
    AUTOMATIC,
    NIGHT_AND_DAY,
    UNSUPPORTED
}

/** Returns the lit status as an enum */
fun createLitStatus(element: Element): LitStatus? = when (element.tags["lit"]) {
    "yes", "lit", "sunset-sunrise", "dusk-dawn", "limited" -> LitStatus.YES
    "no", "unlit" -> LitStatus.NO
    "automatic" -> LitStatus.AUTOMATIC
    "24/7" -> LitStatus.NIGHT_AND_DAY
    null -> when {
        element.tags["indoor"] == "yes" -> LitStatus.YES
        else -> null
    }
    // above tags cover 99.8% of tagged values (2022-02)
    else -> LitStatus.UNSUPPORTED
}
