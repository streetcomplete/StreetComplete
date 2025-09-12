package de.westnordost.streetcomplete.quests.railway_crossing

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRailwayCrossingBarrierForm : AImageListQuestForm<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items: List<RailwayCrossingBarrier> get() {
        val isPedestrian = element.tags["railway"] == "crossing"
        return RailwayCrossingBarrier.getSelectableValues(isPedestrian)
    }

    override val itemsPerRow = 4

    @Composable override fun BoxScope.ItemContent(item: RailwayCrossingBarrier) {
        ImageWithLabel(
            painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            item.title?.let { stringResource(it) }
        )
    }

    override fun onClickOk(selectedItems: List<RailwayCrossingBarrier>) {
        applyAnswer(selectedItems.single())
    }
}
