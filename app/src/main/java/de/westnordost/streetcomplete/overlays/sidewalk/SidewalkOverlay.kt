package de.westnordost.streetcomplete.overlays.sidewalk

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides

class SidewalkOverlay : Overlay {

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.filter("""
            ways with
            highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian
            and area != yes
        """).map { it to getSidewalkStyle(it) } +
        mapData.filter("""
            ways with (
              highway ~ footway|steps
              or highway ~ path|bridleway|cycleway and foot ~ yes|designated
            ) and area != yes
        """).map { it to PolylineStyle("#33cc00") }

    private fun getSidewalkStyle(element: Element): PolylineStyle {
        val sidewalkSides = createSidewalkSides(element.tags)
        if (sidewalkSides == null && sidewalkTaggingNotExpected(element.tags)) {
            return PolylineStyle(null)
        }

        return PolylineStyle(
            color = null,
            colorLeft = sidewalkSides?.left.color,
            colorRight = sidewalkSides?.right.color
        )
    }
}

private fun sidewalkTaggingNotExpected(tags: Map<String, String>): Boolean =
    tags["highway"] == "living_street" || tags["highway"] == "pedestrian"

private val Sidewalk?.color get() = when (this) {
    Sidewalk.YES           -> "#33cc00" // same color as the arrow in the illustrations
    Sidewalk.NO            -> "#555555"
    Sidewalk.SEPARATE      -> Color.INVISIBLE
    Sidewalk.INVALID, null -> Color.UNSPECIFIED
}
