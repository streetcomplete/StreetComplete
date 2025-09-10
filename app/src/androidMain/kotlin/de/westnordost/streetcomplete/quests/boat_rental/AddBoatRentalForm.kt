package de.westnordost.streetcomplete.quests.boat_rental

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBoatRentalForm : AImageListQuestForm<BoatRental, List<BoatRental>>() {

    override val items = BoatRental.entries
    override val itemsPerRow = 3

    override val maxSelectableItems = -1
    override val moveFavoritesToFront = false

    @Composable override fun BoxScope.ItemContent(item: BoatRental) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BoatRental>) {
        applyAnswer(selectedItems)
    }
}
