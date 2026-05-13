package de.westnordost.streetcomplete.quests.powerpoles_material

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddPowerPolesMaterialForm(
    onAnswer: (PowerPolesMaterialAnswer) -> Unit
) {
    ItemSelectQuestForm(
        items = PowerPolesMaterial.entries,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = onAnswer,
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_powerPolesMaterial_is_terminal)) {
                onAnswer(PowerLineAnchoredToBuilding)
            }
        )
    )
}
