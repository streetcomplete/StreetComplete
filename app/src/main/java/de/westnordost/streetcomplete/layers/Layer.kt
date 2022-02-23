package de.westnordost.streetcomplete.layers

import de.westnordost.streetcomplete.data.osm.mapdata.Element

interface Layer {
    /** returns whether the given [element] should be displayed in this layer */
    fun isDisplayed(element: Element): Boolean

    /** returns how to display the given element. Assumes it has been checked first if the element
     *  may be displayed at all. */
    fun getStyle(element: Element): LineStyle
}

data class LineStyle(
    /** color value as hex value, e.g. "#66ff00" */
    val color: String,

    /** dashes style. Null if no dashes */
    val dashes: DashesStyle? = null,

    /** side on which to draw. Null if no side (but center) */
    val side: Side? = null,
) {
    enum class Side { LEFT, RIGHT }
    enum class DashesStyle { DOTTED, DASHED }
}

