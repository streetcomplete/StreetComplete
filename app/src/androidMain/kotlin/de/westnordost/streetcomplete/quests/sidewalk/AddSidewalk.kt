package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_MAJOR_AND_HIGHWAYS
import de.westnordost.streetcomplete.osm.HIGHWAYS
import de.westnordost.streetcomplete.osm.MAJOR_ROADS
import de.westnordost.streetcomplete.osm.PUBLIC_AND_UNCLASSIFIED
import de.westnordost.streetcomplete.osm.TERTIARY
import de.westnordost.streetcomplete.osm.TRUNKS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.UNCLASSIFIED_ROADS
import de.westnordost.streetcomplete.osm.maxspeed.MAX_SPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES

class AddSidewalk : OsmElementQuestType<LeftAndRightSidewalk>, AndroidQuest {
    override val changesetComment = "Specify whether roads have sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            ways with (
                highway ~ path|footway|steps
                or highway ~ cycleway|bridleway and foot ~ yes|designated
              )
              and foot !~ no|private
              and access !~ no|private
        """)

    override val hint = R.string.quest_street_side_puzzle_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        roadsFilter.matches(element)
        && (untaggedRoadsFilter.matches(element) || element.hasInvalidOrIncompleteSidewalkTags())

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: LeftAndRightSidewalk, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}

// streets that may have sidewalk tagging
private val roadsFilter by lazy { """
    ways with
      (
        (
          highway ~ ${(TRUNKS + MAJOR_ROADS + PUBLIC_AND_UNCLASSIFIED + setOf("residential", "service")).joinToString("|")}
          and motorroad != yes
          and expressway != yes
          and foot != no
        )
        or
        (
          highway ~ ${(ALL_MAJOR_AND_HIGHWAYS + PUBLIC_AND_UNCLASSIFIED + setOf("residential", "service")).joinToString("|")}
          and (foot ~ yes|designated or bicycle ~ yes|designated)
        )
      )
      and area != yes
      and access !~ private|no
""".toElementFilterExpression() }

// streets that do not have sidewalk tagging yet
/* the filter additionally filters out ways that are unlikely to have sidewalks:
 *
 * + unpaved roads
 * + roads that are probably not developed enough to have sidewalk (i.e. country roads)
 * + roads with a very low speed limit
 * + Also, anything explicitly tagged as no pedestrians or explicitly tagged that the sidewalk
 *   is mapped as a separate way OR that is tagged with that the cycleway is separate. If the
 *   cycleway is separate, the sidewalk is too for sure
 */
private val untaggedRoadsFilter by lazy { """
    ways with
      highway ~ ${(ALL_MAJOR_AND_HIGHWAYS + UNCLASSIFIED_ROADS + setOf("residential")).joinToString("|")}
      and !sidewalk and !sidewalk:both and !sidewalk:left and !sidewalk:right
      and (!maxspeed or maxspeed > 9 or maxspeed ~ [A-Z].*)
      and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
      and (
        lit = yes
        or highway = residential
        or ~"${MAX_SPEED_TYPE_KEYS.joinToString("|")}" ~ ".*:(urban|.*zone.*|nsl_restricted)"
        or maxspeed <= 60
        or (foot ~ yes|designated and highway ~ ${(ALL_MAJOR_AND_HIGHWAYS - TERTIARY).joinToString("|")})
      )
      and ~foot|bicycle|bicycle:backward|bicycle:forward !~ use_sidepath
      and ~cycleway|cycleway:left|cycleway:right|cycleway:both !~ separate
""".toElementFilterExpression() }

private fun Element.hasInvalidOrIncompleteSidewalkTags(): Boolean {
    val sides = parseSidewalkSides(tags) ?: return false
    if (sides.any { it == INVALID || it == null }) return true
    return false
}
