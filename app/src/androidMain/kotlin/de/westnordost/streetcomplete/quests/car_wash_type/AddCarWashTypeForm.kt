package de.westnordost.streetcomplete.quests.car_wash_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddCarWashTypeForm : AbstractOsmQuestForm<Set<CarWashType>>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemsSelectQuestForm(
            items = CarWashType.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddCarWashTypeForm",
        )
    }
}
