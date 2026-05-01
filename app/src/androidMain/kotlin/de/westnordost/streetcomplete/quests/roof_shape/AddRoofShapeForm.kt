package de.westnordost.streetcomplete.quests.roof_shape

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.MANY
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddRoofShapeForm : AbstractOsmQuestForm<RoofShape>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val items = remember { RoofShape.entries - MANY }
        ItemSelectQuestForm(
            items = items,
            itemsPerRow = 3,
            itemContent = {   Image(painterResource(it.icon), null) },
            onClickOk = { applyAnswer(it) },
            prefs = prefs,
            serializer = serializer(),
            favoriteKey = "AddRoofShapeForm",
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_roofShape_answer_many)) { applyAnswer(MANY) }
            )
        )
    }
}
