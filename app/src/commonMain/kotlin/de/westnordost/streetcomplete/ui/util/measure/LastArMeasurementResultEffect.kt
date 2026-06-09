package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.osm.length.Length

@Composable
fun LastArMeasurementResultEffect(
    lastResult: ArMeasureResult?,
    onMeasureSuccess: (Length) -> Unit,
    onConfirmDisableArQuests: () -> Unit
) {
    var confirmDisableArQuests by remember { mutableStateOf(false) }

    LaunchedEffect(lastResult) {
        confirmDisableArQuests =
            lastResult == ArMeasureResult.Error ||
            // first time don't ask whether to disable (let user install the app first)
            lastResult == ArMeasureResult.NotInstalled && notInstalledCount++ > 0

        if (lastResult is ArMeasureResult.Success) {
            onMeasureSuccess(lastResult.length)
        }
    }

    if (confirmDisableArQuests && lastResult != null) {
        DisableArQuestsDialog(
            onDismissRequest = { confirmDisableArQuests = false },
            onConfirmed = onConfirmDisableArQuests,
            measureResult = lastResult
        )
    }
}

private var notInstalledCount: Int = 0
