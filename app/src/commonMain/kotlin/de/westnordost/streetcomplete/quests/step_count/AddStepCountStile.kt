package de.westnordost.streetcomplete.quests.step_count

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddStepCountStile : OsmElementQuestType<Int> {

    private val stileNodeFilter by lazy { """
        nodes with
          barrier = stile
          and stile ~ stepover|ladder
          and access !~ private|no
          and !step_count
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          access ~ private|no
          and foot !~ permissive|yes|designated
    """.toElementFilterExpression() }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { stileNodeFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!stileNodeFilter.matches(element)) false else null

    override val changesetComment = "Specify stiles step count"
    override val wikiLink = "Key:step_count"
    override val icon = Res.drawable.quest_steps_count_brown
    override val title = Res.string.quest_step_count_title
    override val achievements = listOf(OUTDOORS)
    override val hint = Res.string.quest_step_count_stile_hint

    @Composable
    override fun Form(onAnswer: (QuestAnswer<Int>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        CountInputQuestForm(
            icon = painterResource(Res.drawable.count_step),
            onAnswer = onAnswer
        )
    }

    override fun applyAnswerTo(answer: Int, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["step_count"] = answer.toString()
    }
}
