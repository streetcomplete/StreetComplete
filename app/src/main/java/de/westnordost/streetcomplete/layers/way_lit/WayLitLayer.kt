package de.westnordost.streetcomplete.layers.way_lit

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_PATHS
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.layers.Color
import de.westnordost.streetcomplete.layers.Layer
import de.westnordost.streetcomplete.layers.LineStyle
import de.westnordost.streetcomplete.layers.way_lit.LitStatus.*

class WayLitLayer : Layer {

    private val filter by lazy {
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}".toElementFilterExpression()
    }

    override fun isDisplayed(element: Element) = filter.matches(element)

    override fun getStyle(element: Element) = createLit(element).lineStyle
}

// TODO "show last checked older X as not set" slider? -> controller simply modifies colors -> needs standard colors
// TODO not show private things if unspecified -> simply modify colors -> needs standard colors

val LitStatus.lineStyle: LineStyle get() = when(this) {
    YES -> LineStyle("#ccff33")
    NIGHT_AND_DAY -> LineStyle("#33ff33")
    AUTOMATIC -> LineStyle("#ccff33")
    NO -> LineStyle("#111111")
    UNSPECIFIED -> LineStyle(Color.UNSPECIFIED)
    UNSUPPORTED -> LineStyle(Color.UNSUPPORTED)
}

/** Returns the lit status as an enum */
fun createLit(element: Element): LitStatus = when(element.tags["lit"]) {
    "yes", "lit", "sunset-sunrise", "dusk-dawn" -> YES
    "no", "unlit" -> NO
    "automatic" -> AUTOMATIC
    "24/7" -> NIGHT_AND_DAY
    null -> when {
        element.tags["indoor"] == "yes" -> YES
        else -> UNSPECIFIED
    }
    // above tags cover 99.8% of tagged values (2022-02)
    else -> UNSUPPORTED
}
