package de.westnordost.streetcomplete.layers

import de.westnordost.streetcomplete.data.osm.mapdata.Element

interface Layer {
    /** returns whether the given [element] should be displayed in this layer */
    fun isDisplayed(element: Element): Boolean

    /** returns how to display the given element. Assumes it has been checked first if the element
     *  may be displayed at all. */
    fun getStyle(element: Element): Style
}

