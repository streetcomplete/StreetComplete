package de.westnordost.streetcomplete.quests.religion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.quests.religion.Religion.MULTIFAITH
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddReligionForm(
    onAnswer: (QuestAnswer<Religion>) -> Unit,
    countryInfo: CountryInfo,
) {
    val items = remember {
        val order = countryInfo.popularReligions
            .withIndex()
            .associate { it.value to it.index }
        (Religion.entries - MULTIFAITH).sortedBy { order[it.osmValue] ?: Int.MAX_VALUE }
    }

    ItemSelectQuestForm(
        items = items,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onAnswer = onAnswer,
        otherAnswers = listOf(
            AnswerItem(stringResource(Res.string.quest_religion_for_place_of_worship_answer_multi)) { onAnswer(Answer(MULTIFAITH)) }
        )
    )
}
