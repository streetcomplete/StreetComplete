package de.westnordost.streetcomplete.quests.tourism_information

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInformationForm : AItemSelectQuestForm<TourismInformation, TourismInformation>() {

    override val items = TourismInformation.entries
    override val itemsPerRow = 2
    override val serializer = serializer<TourismInformation>()

    @Composable override fun ItemContent(item: TourismInformation) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: TourismInformation) {
        applyAnswer(selectedItem)
    }
}
