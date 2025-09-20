package de.westnordost.streetcomplete.quests.tourism_information

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInformationForm : AItemSelectQuestForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2
    override val items = TourismInformation.entries

    @Composable override fun ItemContent(item: TourismInformation) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: TourismInformation) {
        applyAnswer(selectedItem)
    }
}
