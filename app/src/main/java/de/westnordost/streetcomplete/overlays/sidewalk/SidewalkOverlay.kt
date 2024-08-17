package de.westnordost.streetcomplete.overlays.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk

class SidewalkOverlay : Overlay {

    override val title = R.string.overlay_sidewalk
    override val icon = R.drawable.ic_quest_sidewalk
    override val changesetComment = "Specify whether roads have sidewalks"
    override val wikiLink: String = "Key:sidewalk"
    override val achievements = listOf(PEDESTRIAN)
    override val hidesQuestTypes = setOf(AddSidewalk::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        // roads
        mapData.filter("""
            ways with
              highway ~ motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian|service
              and area != yes
        """).map { it to getSidewalkStyle(it) } +
        // footways etc, just to highlight e.g. separately mapped sidewalks. However, it is also
        // possible to add sidewalks to them. At least in NL, cycleways with sidewalks actually exist
        mapData.filter("""
            ways with (
              highway ~ footway|steps|path|bridleway|cycleway
            ) and area != yes
        """).map { it to getFootwayStyle(it) }

    override fun createForm(element: Element?): AbstractOverlayForm? {
        if (element == null) return null

        // allow editing of all roads and all exclusive cycleways
        return if (
            element.tags["highway"] in ALL_ROADS ||
            parseSeparateCycleway(element.tags) in listOf(SeparateCycleway.EXCLUSIVE, SeparateCycleway.EXCLUSIVE_WITH_SIDEWALK)
        ) {
            SidewalkOverlayForm()
        } else {
            null
        }
    }
}

private fun getFootwayStyle(element: Element): PolylineStyle {
    val foot = element.tags["foot"] ?: when (element.tags["highway"]) {
        "footway", "steps" -> "designated"
        "path" -> "yes"
        else -> null
    }

    return when {
        parseSidewalkSides(element.tags)?.any { it == Sidewalk.YES } == true ->
            getSidewalkStyle(element)
        foot in listOf("yes", "designated") ->
            PolylineStyle(StrokeStyle(Color.SKY))
        else ->
            PolylineStyle(StrokeStyle(Color.INVISIBLE))
    }
}

private fun getSidewalkStyle(element: Element): PolylineStyle {
    val sidewalks = parseSidewalkSides(element.tags)
    val isNoSidewalkExpected = lazy { sidewalkTaggingNotExpected(element) || isPrivateOnFoot(element) }

    return PolylineStyle(
        stroke = null,
        strokeLeft = sidewalks?.left.getStyle(isNoSidewalkExpected),
        strokeRight = sidewalks?.right.getStyle(isNoSidewalkExpected)
    )
}

private val sidewalkTaggingNotExpectedFilter by lazy { """
    ways with
      highway ~ living_street|pedestrian|service|motorway_link|busway
      or motorroad = yes
      or expressway = yes
      or maxspeed <= 10
      or maxspeed = walk
      or surface ~ ${UNPAVED_SURFACES.joinToString("|")}
      or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(zone)?:?([1-9]|10)"
""".toElementFilterExpression() }

private fun sidewalkTaggingNotExpected(element: Element) =
    sidewalkTaggingNotExpectedFilter.matches(element)

private fun Sidewalk?.getStyle(isNoSidewalkExpected: Lazy<Boolean>) = StrokeStyle(when (this) {
    Sidewalk.YES ->      Color.SKY
    Sidewalk.NO ->       Color.BLACK
    Sidewalk.SEPARATE -> Color.INVISIBLE
    Sidewalk.INVALID  -> Color.DATA_REQUESTED
    null ->              if (isNoSidewalkExpected.value) Color.INVISIBLE else Color.DATA_REQUESTED
})
