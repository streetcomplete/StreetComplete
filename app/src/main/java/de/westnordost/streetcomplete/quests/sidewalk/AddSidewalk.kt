package de.westnordost.streetcomplete.quests.sidewalk

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class AddSidewalk : OsmElementQuestType<LeftAndRightSidewalk> {
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

    // streets that may have sidewalk tagging
    private val roadsFilter by lazy { """
        ways with
          (
            (
              highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|busway
              and motorroad != yes
              and expressway != yes
              and foot != no
            )
            or
            (
              highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|busway
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
          highway ~ ${prefs.getString(questPrefix(prefs) + PREF_SIDEWALK_HIGHWAY_SELECTION, ROADS_WITH_SIDEWALK.joinToString("|"))}
          and (!sidewalk or sidewalk = none) and !sidewalk:both and !sidewalk:left and !sidewalk:right
          and (!maxspeed or maxspeed > 9 or maxspeed ~ [A-Z].*)
          and surface !~ ${UNPAVED_SURFACES.joinToString("|")}
          and (
            lit = yes
            or highway = residential
            or ~"${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}" ~ ".*:(urban|.*zone.*|nsl_restricted)"
            or maxspeed <= 60
            or (foot ~ yes|designated and highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link)
          )
          and ~foot|bicycle|bicycle:backward|bicycle:forward !~ use_sidepath
          and ~cycleway|cycleway:left|cycleway:right|cycleway:both !~ separate
    """.toElementFilterExpression() }
    override val hasQuestSettings = true

    // min distance selection or element selection
    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        singleTypeElementSelectionDialog(context,
            prefs,
            questPrefix(prefs) + PREF_SIDEWALK_HIGHWAY_SELECTION,
            ROADS_WITH_SIDEWALK.joinToString("|"),
            R.string.quest_settings_eligible_highways)
}

private fun Element.hasInvalidOrIncompleteSidewalkTags(): Boolean {
    val sides = parseSidewalkSides(tags) ?: return false
    if (sides.any { it == INVALID || it == null }) return true
    return false
}

private val ROADS_WITH_SIDEWALK = arrayOf(
    "motorway","motorway_link","trunk","trunk_link","primary","primary_link","secondary",
    "secondary_link","tertiary","tertiary_link","unclassified","residential")

private const val PREF_SIDEWALK_HIGHWAY_SELECTION = "qs_AddSidewalk_highway_selection"
