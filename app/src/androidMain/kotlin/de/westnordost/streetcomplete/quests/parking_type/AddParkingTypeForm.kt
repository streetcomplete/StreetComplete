package de.westnordost.streetcomplete.quests.parking_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddParkingTypeForm : AImageListQuestForm<ParkingType, ParkingType>() {

    override val items = ParkingType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: ParkingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<ParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
