package de.westnordost.streetcomplete.quests.orchard_produce

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOrchardProduceForm : AImageListQuestForm<OrchardProduce, List<OrchardProduce>>() {

    private val producesMap = OrchardProduce.entries.associateBy { it.osmValue }
    // only include what is given for that country
    override val items get() = countryInfo.orchardProduces.mapNotNull { producesMap[it] }

    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    @Composable override fun BoxScope.ItemContent(item: OrchardProduce) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<OrchardProduce>) {
        applyAnswer(selectedItems)
    }
}
