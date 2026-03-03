package de.westnordost.streetcomplete.quests.railway_crossing

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRailwayCrossingBarrierForm : AItemSelectQuestForm<RailwayCrossingBarrier, RailwayCrossingBarrier>() {

    override val items: List<RailwayCrossingBarrier> get() {
        val isPedestrian = element.tags["railway"] == "crossing"
        return RailwayCrossingBarrier.getSelectableValues(isPedestrian)
    }
    override val itemsPerRow = 2
    override val serializer = serializer<RailwayCrossingBarrier>()

    @Composable override fun ItemContent(item: RailwayCrossingBarrier) {
        ImageWithLabel(
            painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
            item.title?.let { stringResource(it) }
        )
    }

    override fun onClickOk(selectedItem: RailwayCrossingBarrier) {
        applyAnswer(selectedItem)
    }
}
