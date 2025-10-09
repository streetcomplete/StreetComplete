package de.westnordost.streetcomplete.quests.bike_parking_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeParkingTypeForm : AItemSelectQuestForm<BikeParkingType, BikeParkingType>() {

    override val items = BikeParkingType.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: BikeParkingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: BikeParkingType) {
        applyAnswer(selectedItem)
    }
}
