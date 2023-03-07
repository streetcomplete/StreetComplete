package de.westnordost.streetcomplete.quests.sidewalk

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.osm.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.estimateCycleTrackWidth
import de.westnordost.streetcomplete.osm.estimateParkingOffRoadWidth
import de.westnordost.streetcomplete.osm.estimateRoadwayWidth
import de.westnordost.streetcomplete.osm.guessRoadwayWidth
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.numberSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog
import de.westnordost.streetcomplete.util.math.isNearAndAligned

class AddSidewalk : OsmElementQuestType<LeftAndRightSidewalk> {
    private val maybeSeparatelyMappedSidewalksFilter by lazy { """
        ways with highway ~ path|footway|cycleway|construction and foot != no and access !~ no|private
    """.toElementFilterExpression() }
    // highway=construction included, as situation often changes during and after construction

    override val changesetComment = "Specify whether roads have sidewalks"
    override val wikiLink = "Key:sidewalk"
    override val icon = R.drawable.ic_quest_sidewalk
    override val achievements = listOf(PEDESTRIAN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_overlay

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sidewalk_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val eligibleRoads = mapData.ways.filter { roadsFilter.matches(it) }
        val roadsWithMissingSidewalks = eligibleRoads.filter { untaggedRoadsFilter.matches(it) }.toMutableList()

        /* Unfortunately, the filter above is not enough. In OSM, sidewalks may be mapped as
         * separate ways as well and it is not guaranteed that in this case, sidewalk = separate
         * (or foot = use_sidepath) is always tagged on the main road then. So, all roads should
         * be excluded whose center is within several meters of a footway, to be on the safe side. */

        if (roadsWithMissingSidewalks.isNotEmpty()) {

            val maybeSeparatelyMappedSidewalkGeometries = mapData.ways
                .filter { maybeSeparatelyMappedSidewalksFilter.matches(it) }
                .mapNotNull { mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry }
            if (maybeSeparatelyMappedSidewalkGeometries.isEmpty()) {
                return roadsWithMissingSidewalks
            } else {
                val minAngleToWays = 25.0
                // filter out roads with missing sidewalks that are near footways
                roadsWithMissingSidewalks.removeAll { road ->
                    val minDistToWays = getMinDistanceToWays(road.tags).toDouble()
                    val roadGeometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry
                    roadGeometry?.isNearAndAligned(
                        minDistToWays,
                        minAngleToWays,
                        maybeSeparatelyMappedSidewalkGeometries
                    ) ?: true
                }
            }
        }

        // Also include any roads with invalid (overloaded) or incomplete sidewalk tagging
        val roadsWithInvalidSidewalkTags = eligibleRoads.filter { way ->
            way.hasInvalidOrIncompleteSidewalkTags()
        }

        return roadsWithMissingSidewalks + roadsWithInvalidSidewalkTags
    }

    /* Calculate when footway is too far away from the road to be considered its sidewalk.
       It is an estimate, and we deliberately err on the side of showing the quest too often. */
    private fun getMinDistanceToWays(tags: Map<String, String>): Float =
        (
            (estimateRoadwayWidth(tags) ?: guessRoadwayWidth(tags)) +
            (estimateParkingOffRoadWidth(tags) ?: 0f) +
            (estimateCycleTrackWidth(tags) ?: 0f) +
            1.5f    // assumed sidewalk width
        ) / 2f +
        prefs.getInt(questPrefix(prefs) + PREF_SIDEWALK_DISTANCE, 4).toFloat() // + generous buffer for possible grass verge

    override fun isApplicableTo(element: Element): Boolean? {
        if (!roadsFilter.matches(element)) return false

        /* can't determine for yet untagged roads by the tags alone because we need info about
           surrounding geometry */
        if (untaggedRoadsFilter.matches(element)) return null

        /* but if already tagged and invalid or incomplete, we don't need to look at surrounding
           geometry to see if it is applicable or not */
        return element.hasInvalidOrIncompleteSidewalkTags()
    }

    override fun createForm() = AddSidewalkForm()

    override fun applyAnswerTo(answer: LeftAndRightSidewalk, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(maybeSeparatelyMappedSidewalksFilter)

    // streets that may have sidewalk tagging
    private val roadsFilter by lazy { """
        ways with
          (
            (
              highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service
              and motorroad != yes
              and expressway != yes
              and foot != no
            )
            or
            (
              highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service
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
    * */
    private val untaggedRoadsFilter by lazy { """
        ways with
          highway ~ ${prefs.getString(questPrefix(prefs) + PREF_SIDEWALK_HIGHWAY_SELECTION, ROADS_WITH_SIDEWALK.joinToString("|"))}
          and (!sidewalk or sidewalk = none) and !sidewalk:both and !sidewalk:left and !sidewalk:right
          and (!maxspeed or maxspeed > 9 or maxspeed ~ [A-Z].*)
          and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
          and (
            lit = yes
            or highway = residential
            or ~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")} ~ .*urban|.*zone.*
            or (foot ~ yes|designated and highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link)
          )
          and foot != use_sidepath
          and bicycle != use_sidepath
          and bicycle:backward != use_sidepath
          and bicycle:forward != use_sidepath
          and cycleway != separate
          and cycleway:left != separate
          and cycleway:right != separate
          and cycleway:both != separate
    """.toElementFilterExpression() }
    override val hasQuestSettings = true

    // min distance selection or element selection
    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_settings_sidewalk_what)
            .setPositiveButton(R.string.quest_settings_sidewalk_distance_button) { _,_ ->
                numberSelectionDialog(context,
                    prefs,
                    questPrefix(prefs) + PREF_SIDEWALK_DISTANCE,
                    4,
                    R.string.quest_settings_sidewalk_cycleway_distance_message).show()
            }
            .setNegativeButton(R.string.quest_settings_sidewalk_highways_button) { _, _ ->
                singleTypeElementSelectionDialog(context,
                    prefs,
                    questPrefix(prefs) + PREF_SIDEWALK_HIGHWAY_SELECTION,
                    ROADS_WITH_SIDEWALK.joinToString("|"),
                    R.string.quest_settings_eligible_highways).show()
            }
            .create()
}

private fun Element.hasInvalidOrIncompleteSidewalkTags(): Boolean {
    val sides = createSidewalkSides(tags) ?: return false
    if (sides.any { it == INVALID || it == null }) return true
    return false
}

private val ROADS_WITH_SIDEWALK = arrayOf(
    "motorway","motorway_link","trunk","trunk_link","primary","primary_link","secondary",
    "secondary_link","tertiary","tertiary_link","unclassified","residential")

private const val PREF_SIDEWALK_HIGHWAY_SELECTION = "qs_AddSidewalk_highway_selection"
private const val PREF_SIDEWALK_DISTANCE = "qs_AddSidewalk_distance"
