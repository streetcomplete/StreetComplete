package de.westnordost.streetcomplete.screens.measure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LastArMeasurementResultEffect(
    lastResult: ArMeasureResult?,
    onMeasureSuccess: (Length) -> Unit,
    onConfirmDisableArQuests: () -> Unit
) {
    var confirmDisableArQuestsWithText by remember { mutableStateOf<StringResource?>(null) }

    LaunchedEffect(lastResult) {
        confirmDisableArQuestsWithText = when (lastResult) {
            ArMeasureResult.Error -> {
                Res.string.quest_disable_message_not_working
            }
            ArMeasureResult.NotInstalled -> {
                Res.string.quest_disable_message_not_installed
            }
            is ArMeasureResult.Success -> {
                onMeasureSuccess(lastResult.length)
                null
            }
            else -> null
        }
    }

    confirmDisableArQuestsWithText?.let {
        DisableArQuestsDialog(
            onDismissRequest = { confirmDisableArQuestsWithText = null },
            onConfirmed = onConfirmDisableArQuests,
            text = stringResource(it)
        )
    }
}
