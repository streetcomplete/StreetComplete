package de.westnordost.streetcomplete.quests.boat_rental

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemsSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBoatRentalForm : AbstractOsmQuestForm<Set<BoatRental>>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemsSelectQuestForm(
            items = BoatRental.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddBoatRentalForm",
            moveFavoritesToFront = false,
        )
    }
}
