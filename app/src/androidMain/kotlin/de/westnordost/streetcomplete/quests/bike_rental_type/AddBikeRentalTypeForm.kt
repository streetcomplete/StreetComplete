package de.westnordost.streetcomplete.quests.bike_rental_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBikeRentalTypeForm : AbstractOsmQuestForm<BikeRentalTypeAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = BikeRentalTypeAnswer.entries,
            itemsPerRow = 2,
            itemContent = { item ->
                ImageWithLabel(painterResource(item.icon), stringResource(item.title))
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddBikeRentalTypeForm",
            moveFavoritesToFront = false
        )
    }
}
