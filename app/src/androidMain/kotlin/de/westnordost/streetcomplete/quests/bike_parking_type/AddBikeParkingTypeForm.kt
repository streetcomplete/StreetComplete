package de.westnordost.streetcomplete.quests.bike_parking_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeParkingTypeForm : AImageListQuestForm<BikeParkingType, BikeParkingType>() {

    override val items = BikeParkingType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: BikeParkingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BikeParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
