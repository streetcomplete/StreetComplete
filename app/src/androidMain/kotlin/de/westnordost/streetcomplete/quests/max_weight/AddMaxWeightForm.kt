package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddMaxWeightForm : AbstractOsmQuestForm<List<MaxWeight>>() {

    @Composable
    override fun Content() {
        var signs by rememberSerializable { mutableStateOf(emptyList<MaxWeight>()) }

        var confirmUnusualInput by remember { mutableStateOf(false) }
        var confirmNoSign by remember { mutableStateOf(false) }
        var showUnsupportedSignDialog by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(
                isComplete = signs.isNotEmpty() && signs.all { it.weight != null },
                hasChanges = signs.isNotEmpty(),
                onClick = {
                    if (isUnrealisticWeight(signs)) {
                        confirmUnusualInput = true
                    } else {
                        applyAnswer(signs)
                    }
                }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_maxweight_answer_other_sign)) {
                    showUnsupportedSignDialog = true
                },
                Answer(stringResource(Res.string.quest_generic_answer_noSign)) {
                    confirmNoSign = true
                }
            )
        ) {
            MaxWeightForm(
                signs = signs,
                countryCode = countryInfo.countryCode,
                selectableUnits = countryInfo.weightLimitUnits,
                onSignAdded = { maxweight ->
                    signs = signs.toMutableList().also { it.add(maxweight) }
                },
                onSignRemoved = { index ->
                    signs = signs.toMutableList().also { it.removeAt(index) }
                },
                onSignChanged = { index, maxweight ->
                    signs = signs.toMutableList().also { it[index] = maxweight }
                },
            )
        }

        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { applyAnswer(emptyList()) }
            )
        }
        if (confirmUnusualInput) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmUnusualInput = false },
                onConfirmed = { applyAnswer(signs) },
                text = { Text(stringResource(Res.string.quest_maxweight_unusualInput_confirmation_description)) }
            )
        }
        if (showUnsupportedSignDialog) {
            UnsupportedSignDialog(
                onDismissRequest = { showUnsupportedSignDialog = false },
                onComposeNote = ::composeNote,
                onHideQuest = ::hideQuest
            )
        }
    }
}

private fun isUnrealisticWeight(signs: List<MaxWeight>): Boolean {
    for (sign in signs) {
        val w = sign.weight?.toMetricTons() ?: continue
        if (w > 30 || w < 2) return true
    }
    return false
}

@Composable
private fun UnsupportedSignDialog(
    onDismissRequest: () -> Unit,
    onComposeNote: () -> Unit,
    onHideQuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onComposeNote(); onDismissRequest() }) {
                Text(stringResource(Res.string.ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = { onHideQuest(); onDismissRequest() }) {
                Text(stringResource(Res.string.quest_leave_new_note_no))
            }
        },
        text = { Text(stringResource(Res.string.quest_maxweight_unsupported_sign_request_photo)) },
    )
}
