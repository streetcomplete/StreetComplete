package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBicycleBarrierTypeForm(
    onAnswer: (BicycleBarrierTypeAnswer) -> Unit
) {
    ItemSelectQuestForm(
        items = BicycleBarrierType.entries,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = onAnswer,
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_barrier_bicycle_type_not_cycle_barrier)) {
                onAnswer(BarrierTypeIsNotBicycleBarrier)
            },
        )
    )
}
