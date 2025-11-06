package de.westnordost.streetcomplete.quests.artwork

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental
import de.westnordost.streetcomplete.quests.boat_rental.icon
import de.westnordost.streetcomplete.quests.boat_rental.title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddArtworkTypeForm : AItemsSelectQuestForm<ArtworkType, ArtworkType>() {

    override val items = ArtworkType.entries
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: Set<ArtworkType>) {
        applyAnswer(selectedItems.single())
    }
    override val moveFavoritesToFront = false
    override val serializer = serializer<ArtworkType>()

    @Composable override fun ItemContent(item: ArtworkType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }
}
