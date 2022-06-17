package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element

private val isPrivateOnFootFilter by lazy { """
    nodes, ways, relations with
      access ~ private|no
      and (!foot or foot ~ private|no)
""".toElementFilterExpression() }

fun isPrivateOnFoot(element: Element): Boolean = isPrivateOnFootFilter.matches(element)
