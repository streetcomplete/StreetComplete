package de.westnordost.streetcomplete.quests.parking_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddParkingTypeForm : AItemSelectQuestForm<ParkingType, ParkingType>() {

    override val items = ParkingType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<ParkingType>()

    @Composable override fun ItemContent(item: ParkingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: ParkingType) {
        applyAnswer(selectedItem)
    }
}
