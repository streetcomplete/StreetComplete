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
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Action
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddMaxWeightForm(
    on: (QuestAction<List<MaxWeight>>) -> Unit,
    countryInfo: CountryInfo,
) {
    var signs by rememberSerializable { mutableStateOf(emptyList<MaxWeight>()) }

    var confirmUnusualInput by remember { mutableStateOf(false) }
    var confirmNoSign by remember { mutableStateOf(false) }
    var showUnsupportedSignDialog by remember { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete = signs.isNotEmpty() && signs.all { it.weight != null },
        onClickOk = {
            if (isUnrealisticWeight(signs)) {
                confirmUnusualInput = true
            } else {
                on(Answer(signs))
            }
        },
        hasChanges = signs.isNotEmpty(),
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_maxweight_answer_other_sign)) {
                showUnsupportedSignDialog = true
            },
            AnswerItem(stringResource(Res.string.quest_generic_answer_noSign)) {
                confirmNoSign = true
            }
        ) }
    ) {
        MaxWeightForm(
            signs = signs,
            locale = countryInfo.languageTag?.let { Locale(it) } ?: Locale.current,
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
        AreYouSureDialog(
            onDismissRequest = { confirmNoSign = false },
            onConfirmed = { on(Answer(emptyList())) }
        )
    }
    if (confirmUnusualInput) {
        AreYouSureDialog(
            onDismissRequest = { confirmUnusualInput = false },
            onConfirmed = { on(Answer(signs)) },
            text = { Text(stringResource(Res.string.quest_maxweight_unusualInput_confirmation_description)) }
        )
    }
    if (showUnsupportedSignDialog) {
        UnsupportedSignDialog(
            onDismissRequest = { showUnsupportedSignDialog = false },
            onComposeNote = { on(Action.LeaveNote) },
            onHideQuest = { on(Action.HideQuest) }
        )
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
