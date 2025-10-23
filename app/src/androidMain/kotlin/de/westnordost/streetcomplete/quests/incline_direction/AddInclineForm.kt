package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_steps_incline_up
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInclineForm : AItemSelectQuestForm<Incline, Incline>() {
    override val items = Incline.entries
    override val itemsPerRow = 2
    override val serializer = serializer<Incline>()

    @Composable override fun ItemContent(item: Incline) {
        ImageWithLabel(
            painter = painterResource(item.icon),
            label = stringResource(Res.string.quest_steps_incline_up),
            imageRotation = geometryRotation.floatValue - mapRotation.floatValue
        )
    }

    override fun onClickOk(selectedItem: Incline) {
        applyAnswer(selectedItem)
    }
}
