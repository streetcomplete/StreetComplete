package de.westnordost.streetcomplete.quests.camping

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.jetbrains.compose.resources.stringResource

class AddCampTypeForm : ARadioGroupQuestForm<CampTypeAnswer, Campers>() {

    override val items = Campers.entries

    @Composable override fun BoxScope.ItemContent(item: Campers) {
        Text(stringResource(item.text))
    }

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_camp_type_backcountry) { applyAnswer(CampTypeAnswer.IsBackcountry) },
    )
}
