package de.westnordost.streetcomplete.layers.way_lit

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.layers.Color
import de.westnordost.streetcomplete.layers.Layer
import de.westnordost.streetcomplete.layers.PolylineStyle
import de.westnordost.streetcomplete.layers.StrokeStyle
import de.westnordost.streetcomplete.layers.way_lit.LitStatus.*
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS

class WayLitLayer : Layer {

    private val filter by lazy {
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}".toElementFilterExpression()
    }

    override fun isDisplayed(element: Element) = filter.matches(element)

    override fun getStyle(element: Element) =
        PolylineStyle(StrokeStyle(createLitStatus(element).color))
}

// TODO LAYERS "show last checked older X as not set" slider? -> controller simply modifies colors -> needs standard colors

private val LitStatus.color: String get() = when (this) {
    YES ->           "#ccff00"
    NIGHT_AND_DAY -> "#33ff00"
    AUTOMATIC ->     "#00aaff"
    NO ->            "#333333"
    UNSPECIFIED ->   Color.UNSPECIFIED
    UNSUPPORTED ->   Color.UNSUPPORTED
}
