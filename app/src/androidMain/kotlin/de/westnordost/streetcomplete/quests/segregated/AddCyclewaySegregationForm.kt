package de.westnordost.streetcomplete.quests.segregated

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCyclewaySegregationForm : AImageListQuestForm<CyclewaySegregation, CyclewaySegregation>() {

    override val items = CyclewaySegregation.entries
    override val itemsPerRow = 1

    @Composable override fun BoxScope.ItemContent(item: CyclewaySegregation) {
        ImageWithDescription(
            painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            title = null,
            description = stringResource(item.title)
        )
    }

    override fun onClickOk(selectedItems: List<CyclewaySegregation>) {
        applyAnswer(selectedItems.single())
    }
}
