package de.westnordost.streetcomplete.quests.segregated

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCyclewaySegregationForm : AItemSelectQuestForm<CyclewaySegregation, CyclewaySegregation>() {

    override val items = CyclewaySegregation.entries
    override val itemsPerRow = 1

    @Composable override fun ItemContent(item: CyclewaySegregation) {
        ImageWithDescription(
            painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            title = null,
            description = stringResource(item.title)
        )
    }

    override fun onClickOk(selectedItem: CyclewaySegregation) {
        applyAnswer(selectedItem)
    }
}
