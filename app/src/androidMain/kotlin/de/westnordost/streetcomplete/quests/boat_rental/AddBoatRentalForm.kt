package de.westnordost.streetcomplete.quests.boat_rental

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBoatRentalForm : AItemsSelectQuestForm<BoatRental, Set<BoatRental>>() {

    override val items = BoatRental.entries
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false
    override val serializer = serializer<BoatRental>()

    @Composable override fun ItemContent(item: BoatRental) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<BoatRental>) {
        applyAnswer(selectedItems)
    }
}
