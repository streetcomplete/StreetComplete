package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags

class AddPathSmoothness : OsmFilterQuestType<SmoothnessAnswer>() {

    override val elementFilter = """
        ways with
          highway ~ ${ALL_PATHS_EXCEPT_STEPS.joinToString("|")}
          and surface ~ ${SURFACES_FOR_SMOOTHNESS.joinToString("|")}
          and access !~ private|no
          and segregated != yes
          and (!conveying or conveying = no)
          and (!indoor or indoor = no)
          and !cycleway:surface and !footway:surface
          and (
            !smoothness
            or smoothness older today -4 years
            or smoothness:date < today -4 years
          )
    """
    override val changesetComment = "Specify paths smoothness"
    override val wikiLink = "Key:smoothness"
    override val icon = R.drawable.ic_quest_way_surface_detail
    override val achievements = listOf(WHEELCHAIR, BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_smoothness_title2

    override fun getTitleArgs(tags: Map<String, String>): Array<String>
        = arrayOf(tags["surface"].toString())

    override fun createForm() = AddSmoothnessForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val nodes = (element as Way).nodeIds
        return getMapData().nodes.asSequence().filter { it.id in nodes && barrierFilter.matches(it) }
    }

    private val barrierFilter by lazy {
        "nodes with barrier or traffic_calming or (kerb and kerb !~ no|flush)".toElementFilterExpression()
    }

    override fun applyAnswerTo(answer: SmoothnessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}

// smoothness is not asked for steps
val ALL_PATHS_EXCEPT_STEPS = listOf("footway", "cycleway", "path", "bridleway")
