package de.westnordost.streetcomplete.quests.crossing_markings

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddCrossingMarkings : OsmElementQuestType<Boolean> {

    private val crossingFilter by lazy { """
        nodes with highway = crossing
          and foot != no
          and (
            !crossing:markings and crossing !~ uncontrolled|zebra|marked|unmarked
            or crossing:markings older today -8 years
          )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway = service and service = driveway
          or highway and access ~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether pedestrian crossings have markings"
    override val wikiLink = "Key:crossing:markings"
    override val icon = R.drawable.ic_quest_pedestrian_crossing
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)

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

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val crossingMarkings = tags["crossing:markings"]
        // don't overwrite a more specific answer (e.g. crossing:markings = zebra)
        if (answer && (crossingMarkings != null && crossingMarkings != "no")) {
            tags.updateCheckDateForKey("crossing:markings")
        } else {
            tags.updateWithCheckDate("crossing:markings", answer.toYesNo())
        }

        val crossing = tags["crossing"]
        // delete crossing tag only if new answer directly conflicts with crossing value
        if (crossing != null) {
            val hasConflict = when (answer) {
                false -> crossing in listOf("uncontrolled","zebra","marked")
                true -> crossing in listOf("unmarked")
            }
            if (hasConflict) {
                tags.remove("crossing")
                tags.removeCheckDatesForKey("crossing")
            }
        }
    }
}

// TODO TESTS
