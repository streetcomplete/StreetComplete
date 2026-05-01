package de.westnordost.streetcomplete.quests.tracktype

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.barrier_type.icon
import de.westnordost.streetcomplete.quests.barrier_type.title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddTracktypeForm : AbstractOsmQuestForm<Tracktype>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = Tracktype.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddTracktypeForm",
            moveFavoritesToFront = false,
        )
    }
}
