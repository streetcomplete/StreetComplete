package de.westnordost.streetcomplete.quests.tourism_information

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddInformationForm : AImageListQuestForm<TourismInformation, TourismInformation>() {

    override val itemsPerRow = 2
    override val items = TourismInformation.entries

    @Composable override fun BoxScope.ItemContent(item: TourismInformation) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<TourismInformation>) {
        applyAnswer(selectedItems.single())
    }
}
