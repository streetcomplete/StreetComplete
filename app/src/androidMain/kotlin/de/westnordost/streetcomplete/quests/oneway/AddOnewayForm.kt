package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOnewayForm : AItemSelectQuestForm<OnewayAnswer, OnewayAnswer>() {

    override val items = OnewayAnswer.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: OnewayAnswer) {
        val painter = painterResource(item.icon)
        ImageWithLabel(
            painter = remember(painter) { ClipCirclePainter(painter) },
            label = stringResource(item.title),
            imageRotation = geometryRotation.floatValue - mapRotation.floatValue
        )
    }

    override fun onClickOk(selectedItem: OnewayAnswer) {
        applyAnswer(selectedItem)
    }
}
