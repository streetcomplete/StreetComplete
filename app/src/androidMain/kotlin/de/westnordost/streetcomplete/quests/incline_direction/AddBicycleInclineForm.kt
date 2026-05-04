package de.westnordost.streetcomplete.quests.incline_direction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddBicycleInclineForm : AbstractOsmQuestForm<BicycleInclineAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        var confirmUpAndDown by remember { mutableStateOf(false) }

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
            onClickOk = { applyAnswer(RegularBicycleInclineAnswer(it)) },
            prefs = prefs,
            favoriteKey = "AddBicycleInclineForm",
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_bicycle_incline_up_and_down)) {
                    confirmUpAndDown = true
                }
            )
        )

        if (confirmUpAndDown) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmUpAndDown = false },
                onConfirmed = { applyAnswer(UpdAndDownHopsAnswer) }
            )
        }
    }
}
