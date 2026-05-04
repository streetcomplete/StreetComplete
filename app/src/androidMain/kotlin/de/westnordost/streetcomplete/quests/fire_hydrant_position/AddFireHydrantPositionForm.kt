package de.westnordost.streetcomplete.quests.fire_hydrant_position

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddFireHydrantPositionForm : AbstractOsmQuestForm<FireHydrantPosition>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = FireHydrantPosition.entries,
            itemsPerRow = 2,
            itemContent = { item ->
                val isPillar = element.tags["fire_hydrant:type"] == "pillar"
                ImageWithLabel(painterResource(item.getIcon(isPillar)), stringResource(item.title))
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            favoriteKey = "AddFireHydrantPositionForm",
        )
    }
}
