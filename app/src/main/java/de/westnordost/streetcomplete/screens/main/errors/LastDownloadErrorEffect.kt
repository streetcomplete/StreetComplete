package de.westnordost.streetcomplete.screens.main.errors

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.util.ktx.toast

/** Depending on the type of error, either display or conditionally offer to report the last
 *  occurred error during download */
@Composable
fun LastDownloadErrorEffect(
    lastError: Exception,
    onReportError: (error: Exception) -> Unit
) {
    val context = LocalContext.current

    var showDownloadErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastError) {
        when (lastError) {
            is ConnectionException -> {
                context.toast(R.string.download_server_error, Toast.LENGTH_LONG)
            }
            is AuthorizationException -> {
                context.toast(R.string.auth_error, Toast.LENGTH_LONG)
            }
            else -> {
                showDownloadErrorDialog = true
            }
        }
    }

    if (showDownloadErrorDialog) {
        SendErrorReportDialog(
            onDismissRequest = { showDownloadErrorDialog = false },
            onConfirmed = { onReportError(lastError) },
            title = stringResource(R.string.download_error)
        )
    }
}
