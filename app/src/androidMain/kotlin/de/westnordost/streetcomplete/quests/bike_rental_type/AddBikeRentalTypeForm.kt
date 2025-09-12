package de.westnordost.streetcomplete.quests.bike_rental_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeRentalTypeForm : AImageListQuestForm<BikeRentalTypeAnswer, BikeRentalTypeAnswer>() {

    override val items = BikeRentalType.entries + BikeRentalTypeAnswer.BikeShopWithRental
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    @Composable override fun BoxScope.ItemContent(item: BikeRentalTypeAnswer) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BikeRentalTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
