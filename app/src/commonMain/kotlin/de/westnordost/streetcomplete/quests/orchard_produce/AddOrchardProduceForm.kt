package de.westnordost.streetcomplete.quests.orchard_produce

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOrchardProduceForm(
    on: (QuestAction<Set<OrchardProduce>>) -> Unit,
    countryInfo: CountryInfo
) {
    val items = remember {
        val order = countryInfo.orchardProduces
            .withIndex()
            .associate { it.value to it.index }
        OrchardProduce.entries.sortedBy { order[it.osmValue] ?: Int.MAX_VALUE }
    }

    ItemsSelectQuestForm(
        on = on,
        items = items,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
    )
}
