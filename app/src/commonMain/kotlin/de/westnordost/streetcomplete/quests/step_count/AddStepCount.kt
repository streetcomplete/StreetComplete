package de.westnordost.streetcomplete.quests.step_count

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.CountInputQuestForm
import org.jetbrains.compose.resources.painterResource

class AddStepCount : OsmFilterQuestType<Int>() {

    override val elementFilter = """
        nodes, ways with
        (
          (
            highway = steps
            and (!indoor or indoor = no)
            and (!conveying or conveying = no)
          )
          or man_made = tower and access ~ yes|customers and tower:type ~ observation|watchtower
        )
        and access !~ private|no
        and !step_count
    """
    override val changesetComment = "Specify step counts"
    override val wikiLink = "Key:step_count"
    override val icon = Res.drawable.quest_steps_count
    override val title = Res.string.quest_step_count_title
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true
    override val achievements = listOf(PEDESTRIAN)

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
