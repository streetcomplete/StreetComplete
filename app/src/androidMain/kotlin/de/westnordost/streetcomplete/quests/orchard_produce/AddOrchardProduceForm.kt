package de.westnordost.streetcomplete.quests.orchard_produce

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddOrchardProduceForm : AbstractOsmQuestForm<Set<OrchardProduce>>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val items = remember {
            val producesMap = OrchardProduce.entries.associateBy { it.osmValue }
            // only include what is given for that country
            countryInfo.orchardProduces.mapNotNull { producesMap[it] }
        }

        ItemsSelectQuestForm(
            items = items,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddOrchardProduceForm",
        )
    }
}
