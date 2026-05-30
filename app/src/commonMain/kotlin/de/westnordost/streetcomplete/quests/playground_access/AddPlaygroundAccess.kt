package de.westnordost.streetcomplete.quests.playground_access

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class AddPlaygroundAccess : OsmFilterQuestType<PlaygroundAccess>() {

    override val elementFilter = """
        nodes, ways, relations with
          leisure = playground
          and (!access or access = unknown)
    """
    override val changesetComment = "Specify access to playgrounds"
    override val wikiLink = "Tag:leisure=playground"
    override val icon = Res.drawable.quest_playground
    override val title = Res.string.quest_playground_access_title2
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<PlaygroundAccess>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            items = PlaygroundAccess.entries,
            itemContent = { Text(stringResource(it.text)) },
            onAnswer = onAnswer
        )
    }

    override fun applyAnswerTo(answer: PlaygroundAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
