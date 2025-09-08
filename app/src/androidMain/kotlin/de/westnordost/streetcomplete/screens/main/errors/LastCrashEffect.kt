package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.crash_title
import org.jetbrains.compose.resources.stringResource

/** Offer to report the last occurred crash */
@Composable
fun LastCrashEffect(
    lastReport: String,
    onReport: (errorReport: String) -> Unit
) {
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastReport) { showErrorDialog = true }

    if (showErrorDialog) {
        SendErrorReportDialog(
            onDismissRequest = { showErrorDialog = false },
            onConfirmed = { onReport(lastReport) },
            title = stringResource(Res.string.crash_title)
        )
    }
}
