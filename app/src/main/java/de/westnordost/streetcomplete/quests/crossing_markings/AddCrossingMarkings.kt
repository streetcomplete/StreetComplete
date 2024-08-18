package de.westnordost.streetcomplete.quests.crossing_markings

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

class AddCrossingMarkings : OsmElementQuestType<CrossingMarkings> {

    private val crossingFilter by lazy { """
        nodes with
          highway = crossing
          and foot != no
          and $crossingMarkingExpression
          and (!crossing:signals or crossing:signals = no)
    """.toElementFilterExpression() }

    /* only looking for crossings that have no crossing=* at all set because if the crossing was
     * - if it had markings, it would be tagged with "marked","zebra" or "uncontrolled"
     * - if it hadn't, it would be tagged with "unmarked"
     * - and in case of "traffic_signals", we currently assume that when there are traffic signals
     *   it would be spammy to ask about markings because the answer would almost always be "yes".
     *   Might differ per country, research necessary. */

    private val excludedWaysFilter by lazy { """
        ways with
          highway and access ~ private|no
          or highway = service and service = driveway
    """.toElementFilterExpression() }

    override val changesetComment = "Specify type or existence of pedestrian crossing markings"
    override val wikiLink = "Key:crossing:markings"
    override val icon = R.drawable.ic_quest_pedestrian_crossing
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_pedestrian_crossing_markings

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossing() }.asSequence()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    override fun createForm() =
        if (prefs.getBoolean(PREF_CROSSING_MARKING_EXTENDED, false)) {
            AddCrossingMarkingsForm()
        } else {
            AddCrossingMarkingsYesNoForm()
        }

    override fun applyAnswerTo(answer: CrossingMarkings, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["crossing:markings"] = answer.osmValue
    }

    override val hasQuestSettings: Boolean = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setMessage(R.string.pref_quest_pedestrian_crossing_markings_extended)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ ->
                prefs.edit().putBoolean(PREF_CROSSING_MARKING_EXTENDED, true).apply()
            }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ ->
                prefs.edit().putBoolean(PREF_CROSSING_MARKING_EXTENDED, false).apply()
            }
            .create()

    private val crossingMarkingExpression = if (prefs.getBoolean(PREF_CROSSING_MARKING_EXTENDED, false)) {
        "( !crossing:markings or crossing:markings=yes )"
    } else {
        "!crossing:markings and (!crossing or crossing = island )"
    }
}

private const val PREF_CROSSING_MARKING_EXTENDED = "qs_AddCrossingMarkings_extended"
