package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_steps_incline_up
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddInclineForm : AbstractOsmQuestForm<Incline>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = Incline.entries,
            itemsPerRow = 2,
            itemContent = { item ->
                ImageWithLabel(
                    painter = painterResource(item.icon),
                    label = stringResource(Res.string.quest_steps_incline_up),
                    imageRotation = geometryRotation.floatValue - mapRotation.floatValue
                )
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddInclineForm",
        )
    }
}
