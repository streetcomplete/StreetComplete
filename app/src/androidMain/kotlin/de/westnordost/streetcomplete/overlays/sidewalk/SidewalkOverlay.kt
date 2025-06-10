package de.westnordost.streetcomplete.overlays.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk

class SidewalkOverlay : Overlay, AndroidOverlay {

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
              highway ~ ${ALL_ROADS.joinToString("|")}
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

private fun getFootwayStyle(element: Element): OverlayStyle.Polyline {
    val foot = element.tags["foot"] ?: when (element.tags["highway"]) {
        "footway", "steps" -> "designated"
        "path" -> "yes"
        else -> null
    }

    return when {
        parseSidewalkSides(element.tags)?.any { it == Sidewalk.YES } == true ->
            getSidewalkStyle(element)
        foot in listOf("yes", "designated") ->
            OverlayStyle.Polyline(OverlayStyle.Stroke(OverlayColor.Sky))
        else ->
            OverlayStyle.Polyline(OverlayStyle.Stroke(OverlayColor.Invisible))
    }
}

private fun getSidewalkStyle(element: Element): OverlayStyle.Polyline {
    val sidewalks = parseSidewalkSides(element.tags)
    val isNoSidewalkExpected = lazy { sidewalkTaggingNotExpected(element) || isPrivateOnFoot(element) }

    return OverlayStyle.Polyline(
        stroke = getStreetStrokeStyle(element.tags),
        strokeLeft = sidewalks?.left.getStyle(isNoSidewalkExpected),
        strokeRight = sidewalks?.right.getStyle(isNoSidewalkExpected)
    )
}

private fun getStreetStrokeStyle(tags: Map<String, String>): OverlayStyle.Stroke? =
    when {
        tags["highway"] == "pedestrian" ->
            OverlayStyle.Stroke(OverlayColor.Sky)
        tags["highway"] == "living_street" || tags["living_street"] == "yes" ->
            OverlayStyle.Stroke(OverlayColor.Sky, dashed = true)
        else -> null
    }

private val sidewalkTaggingNotExpectedFilter by lazy { """
    ways with
      highway ~ track|living_street|pedestrian|service|motorway_link|motorway|busway
      or motorroad = yes
      or expressway = yes
      or maxspeed <= 10
      or maxspeed = walk
      or surface ~ ${UNPAVED_SURFACES.joinToString("|")}
      or ~"${MAX_SPEED_TYPE_KEYS.joinToString("|")}" ~ ".*:(zone)?:?([1-9]|10)"
""".toElementFilterExpression() }

private fun sidewalkTaggingNotExpected(element: Element) =
    sidewalkTaggingNotExpectedFilter.matches(element)

private fun Sidewalk?.getStyle(isNoSidewalkExpected: Lazy<Boolean>) = OverlayStyle.Stroke(when (this) {
    Sidewalk.YES ->      OverlayColor.Sky
    Sidewalk.NO ->       OverlayColor.Black
    Sidewalk.SEPARATE -> OverlayColor.Invisible
    Sidewalk.INVALID  -> OverlayColor.Red
    null ->              if (isNoSidewalkExpected.value) OverlayColor.Invisible else OverlayColor.Red
})
