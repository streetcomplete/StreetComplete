package de.westnordost.streetcomplete.quests.railway_crossing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddRailwayCrossingBarrierForm : AbstractOsmQuestForm<RailwayCrossingBarrier>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val items = remember {
            val isPedestrian = element.tags["railway"] == "crossing"
            RailwayCrossingBarrier.getSelectableValues(isPedestrian)
        }

        ItemSelectQuestForm(
            items = items,
            itemsPerRow = 2,
            itemContent = { item ->
                ImageWithLabel(
                    painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
                    item.title?.let { stringResource(it) }
                )
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddRailwayCrossingBarrierForm",
        )
    }
}
