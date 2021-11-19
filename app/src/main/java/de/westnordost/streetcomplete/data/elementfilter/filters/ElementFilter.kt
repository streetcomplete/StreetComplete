package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.data.elementfilter.Matcher
import de.westnordost.streetcomplete.data.osm.mapdata.Element

sealed interface ElementFilter : Matcher<Element> {
    abstract override fun toString(): String
}
