package de.westnordost.streetcomplete.quests.kerb_height

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddKerbHeightForm(
    onAnswer: (KerbHeight) -> Unit
) {
    ItemSelectQuestForm(
        items = KerbHeight.entries,
        itemsPerRow = 2,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        onClickOk = onAnswer
    )
}
