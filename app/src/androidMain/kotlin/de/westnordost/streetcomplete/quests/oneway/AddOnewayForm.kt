package de.westnordost.streetcomplete.quests.oneway

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddOnewayForm : AbstractOsmQuestForm<OnewayAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectQuestForm(
            items = OnewayAnswer.entries,
            itemContent = { item ->
                val painter = painterResource(item.icon)
                ImageWithLabel(
                    painter = remember(painter) { ClipCirclePainter(painter) },
                    label = stringResource(item.title),
                    imageRotation = geometryRotation.floatValue - mapRotation.floatValue
                )
            },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddOnewayForm",
            itemsPerRow = 3
        )
    }
}
