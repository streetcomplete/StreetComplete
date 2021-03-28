package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.elementfilter.Matcher

interface ElementFilter : Matcher<Element> {
    fun toOverpassQLString(): String
}
