package de.westnordost.streetcomplete.quests.orchard_produce

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddOrchardProduceForm : AItemsSelectQuestForm<OrchardProduce, Set<OrchardProduce>>() {

    private val producesMap = OrchardProduce.entries.associateBy { it.osmValue }
    // only include what is given for that country
    override val items get() = countryInfo.orchardProduces.mapNotNull { producesMap[it] }
    override val itemsPerRow = 3
    override val serializer = serializer<OrchardProduce>()

    @Composable override fun ItemContent(item: OrchardProduce) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: Set<OrchardProduce>) {
        applyAnswer(selectedItems)
    }
}
