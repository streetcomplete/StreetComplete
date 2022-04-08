package de.westnordost.streetcomplete.layers.sidewalk

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.layers.Color
import de.westnordost.streetcomplete.layers.Layer
import de.westnordost.streetcomplete.layers.PolylineStyle
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides

class SidewalkLayer : Layer {

    private val filter by lazy {
        "ways with highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian".toElementFilterExpression()
    }

    override fun isDisplayed(element: Element) = filter.matches(element)

    override fun getStyle(element: Element): PolylineStyle {
        val sidewalkSides = createSidewalkSides(element.tags)
        if (sidewalkSides == null && !expectSidewalkTagging(element.tags))
            return PolylineStyle(null)

        return PolylineStyle(
            color = null,
            colorLeft = sidewalkSides?.left?.color,
            colorRight = sidewalkSides?.right.color
        )
    }
}

private fun expectSidewalkTagging(tags: Map<String, String>): Boolean =
    tags["highway"] != "living_street" && tags["highway"] != "pedestrian"

private val Sidewalk?.color get() = when (this) {
    Sidewalk.YES           -> "#33cc00" // same color as the arrow in the illustrations
    Sidewalk.NO            -> "#555555"
    Sidewalk.SEPARATE      -> "#00aaff" // TODO LAYERS for some reason with transparency will not render. This is an issue because separately mapped sidewalk may be behind it
    Sidewalk.INVALID, null -> Color.UNSPECIFIED
}
