package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_cycleway_direction_dir
import de.westnordost.streetcomplete.resources.quest_cycleway_direction_no_oneway
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCyclewayDirectionForm : AItemSelectQuestForm<OnewayAnswer, OnewayAnswer>() {

    override val items = OnewayAnswer.entries
    override val itemsPerRow = 3
    override val serializer = serializer<OnewayAnswer>()

    @Composable override fun ItemContent(item: OnewayAnswer) {
        val painter = painterResource(item.icon)
        ImageWithLabel(
            painter = remember(painter) { ClipCirclePainter(painter) },
            label = stringResource(item.cyclewayDirectionTitle),
            imageRotation = geometryRotation.floatValue - mapRotation.floatValue,
        )
    }

    override fun onClickOk(selectedItem: OnewayAnswer) {
        applyAnswer(selectedItem)
    }
}

private val OnewayAnswer.cyclewayDirectionTitle: StringResource get() = when (this) {
    OnewayAnswer.FORWARD, OnewayAnswer.BACKWARD -> Res.string.quest_cycleway_direction_dir
    OnewayAnswer.NO_ONEWAY -> Res.string.quest_cycleway_direction_no_oneway
}
