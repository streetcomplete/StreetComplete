package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddRoadSmoothness : OsmFilterQuestType<SmoothnessAnswer>() {

    override val elementFilter = """
        ways with (
            highway ~ ${ROADS_TO_ASK_SMOOTHNESS_FOR.joinToString("|")}
            or highway = service and service !~ driveway|slipway
          )
          and surface ~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}
          and (access !~ private|no or (foot and foot !~ private|no))
          and (
            !smoothness
            or smoothness older today -4 years
            or smoothness:date < today -4 years
          )
    """

    override val changesetComment = "Specify road smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_street_surface_detail
    override val achievements = listOf(CAR, BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) =
        if (tags["area"] == "yes") {
            R.string.quest_smoothness_square_title
        } else {
            R.string.quest_smoothness_road_title
        }

    override fun createForm() = AddSmoothnessForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val nodes = (element as Way).nodeIds
        return getMapData().nodes.asSequence().filter { it.id in nodes && barrierFilter.matches(it) }
    }

    private val barrierFilter by lazy {
        "nodes with barrier or traffic_calming".toElementFilterExpression()
    }

    override fun applyAnswerTo(answer: SmoothnessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is IsActuallyStepsAnswer) throw IllegalStateException()
        answer.applyTo(tags)
    }
}

// surfaces that are actually used in AddSmoothnessForm
// should only contain values that are in the Surface class
val SURFACES_FOR_SMOOTHNESS = listOf(
    "asphalt", "concrete", "concrete:plates", "sett", "paving_stones", "compacted", "gravel", "fine_gravel"
)

private val ROADS_TO_ASK_SMOOTHNESS_FOR = arrayOf(
    // "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition smooth asphalt (or concrete)
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian", "track",
    // "service", // this is too much, and the information value is very low
)
