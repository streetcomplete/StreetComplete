package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBicycleInclineForm(
    onAnswer: (BicycleInclineAnswer) -> Unit,
) {
    var confirmUpAndDown by remember { mutableStateOf(false) }

    ItemSelectQuestForm(
        items = Incline.entries,
        itemsPerRow = 2,
        itemContent = { item ->
            ImageWithLabel(
                painter = painterResource(item.icon),
                label = stringResource(Res.string.quest_steps_incline_up),
                imageRotation = geometryRotation.floatValue - LocalMapRotation.current
            )
        },
        onClickOk = { onAnswer(RegularBicycleInclineAnswer(it)) },
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_bicycle_incline_up_and_down)) {
                confirmUpAndDown = true
            }
        )
    )

    if (confirmUpAndDown) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmUpAndDown = false },
            onConfirmed = { onAnswer(UpdAndDownHopsAnswer) }
        )
    }
}
