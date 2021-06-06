package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.Matcher
import de.westnordost.streetcomplete.data.osm.mapdata.Element

interface ElementFilter : Matcher<Element> {
    fun toOverpassQLString(): String
}
