package de.westnordost.streetcomplete.quests.traffic_calming_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTrafficCalmingTypeForm : AItemSelectQuestForm<TrafficCalmingType, TrafficCalmingType>() {

    override val items = TrafficCalmingType.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: TrafficCalmingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: TrafficCalmingType) {
        applyAnswer(selectedItem)
    }
}
