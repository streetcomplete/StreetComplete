package de.westnordost.streetcomplete.quests.bridge_structure

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.koin.android.ext.android.inject

class AddBridgeStructureForm : AbstractOsmQuestForm<BridgeStructure>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = BridgeStructure.entries,
            itemsPerRow = 1,
            itemContent = { Image(painterResource(it.icon), null) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddBridgeStructureForm",
        )
    }
}
