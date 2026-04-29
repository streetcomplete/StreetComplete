package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddMaxHeightForm : AbstractOsmQuestForm<MaxHeightAnswer>() {

    @Composable
    override fun Content() {
        var height by rememberSerializable { mutableStateOf<Length?>(null) }

        var confirmUnusualInput by remember { mutableStateOf(false) }
        var confirmNoSign by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(
                isComplete = height != null,
                onClick = {
                    if (isUnrealisticHeight(height!!)) {
                        confirmUnusualInput = true
                    } else {
                        applyAnswer(MaxHeight(height!!))
                    }
                }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_maxheight_answer_noSign)) { confirmNoSign = true }
            ),
            hintText =
                if (element.type == ElementType.WAY) {
                    stringResource(Res.string.quest_maxheight_split_way_hint,
                        stringResource(Res.string.quest_generic_answer_differs_along_the_way)
                    )
                } else {
                    null
                },
        ) {
            MaxHeightForm(
                length = height,
                selectableUnits = countryInfo.lengthUnits,
                onChange = { height = it },
                countryCode = countryInfo.countryCode,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { applyAnswer(NoMaxHeightSign) }
            )
        }
        if (confirmUnusualInput) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmUnusualInput = false },
                onConfirmed = { applyAnswer(MaxHeight(height!!)) },
                text = { Text(stringResource(Res.string.quest_maxheight_unusualInput_confirmation_description)) }
            )
        }
    }
}

private fun isUnrealisticHeight(length: Length): Boolean {
    val m = length.toMeters()
    return m > 6 || m < 1.8
}
