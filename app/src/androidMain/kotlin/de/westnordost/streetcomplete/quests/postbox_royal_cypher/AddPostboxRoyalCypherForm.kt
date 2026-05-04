package de.westnordost.streetcomplete.quests.postbox_royal_cypher

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.koin.android.ext.android.inject

class AddPostboxRoyalCypherForm : AbstractOsmQuestForm<PostboxRoyalCypher>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = PostboxRoyalCypher.entries,
            itemsPerRow = 3,
            itemContent = { ImageWithLabel(painterResource(it.icon), it.title) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddPostboxRoyalCypherForm",
        )
    }
}
