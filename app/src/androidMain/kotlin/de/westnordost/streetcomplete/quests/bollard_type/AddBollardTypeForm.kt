package de.westnordost.streetcomplete.quests.bollard_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBollardTypeForm(
    onAnswer: (BollardTypeAnswer) -> Unit,
) {
    ItemSelectQuestForm(
        items = BollardType.entries,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = onAnswer,
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_bollard_type_not_bollard)) {
                onAnswer(BarrierTypeIsNotBollard)
            },
        )
    )
}
