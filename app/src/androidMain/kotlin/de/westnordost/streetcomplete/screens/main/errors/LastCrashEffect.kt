package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R

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
            title = stringResource(R.string.crash_title)
        )
    }
}
