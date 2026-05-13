package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOnewayForm(
    onAnswer: (OnewayAnswer) -> Unit
) {
    ItemSelectQuestForm(
        items = OnewayAnswer.entries,
        itemContent = { item ->
            val painter = painterResource(item.icon)
            ImageWithLabel(
                painter = remember(painter) { ClipCirclePainter(painter) },
                label = stringResource(item.title),
                imageRotation = geometryRotation.floatValue - LocalMapRotation.current
            )
        },
        onClickOk = onAnswer,
    )
}
