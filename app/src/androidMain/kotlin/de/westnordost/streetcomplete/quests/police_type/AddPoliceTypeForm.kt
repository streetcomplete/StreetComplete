package de.westnordost.streetcomplete.quests.police_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.koin.android.ext.android.inject

class AddPoliceTypeForm : AbstractOsmQuestForm<PoliceType>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = PoliceType.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), it.title) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddPoliceTypeForm",
        )
    }
}
