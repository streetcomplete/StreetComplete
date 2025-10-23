package de.westnordost.streetcomplete.quests.bike_rental_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeRentalTypeForm : AItemSelectQuestForm<BikeRentalTypeAnswer, BikeRentalTypeAnswer>() {

    override val items = BikeRentalType.entries + BikeRentalTypeAnswer.BikeShopWithRental
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false
    override val serializer = serializer<BikeRentalTypeAnswer>()

    @Composable override fun ItemContent(item: BikeRentalTypeAnswer) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: BikeRentalTypeAnswer) {
        applyAnswer(selectedItem)
    }
}
