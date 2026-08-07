package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ToastPopup
import org.jetbrains.compose.resources.stringResource

/** Depending on the type of error, either display or conditionally offer to report the last
 *  occurred error during download */
@Composable
fun LastDownloadErrorEffect(
    lastError: Exception,
    onReportError: (error: Exception) -> Unit
) {
    var showDownloadErrorDialog by remember { mutableStateOf(false) }
    var showServerError by remember { mutableStateOf<Boolean>(false) }
    var showAuthError by remember { mutableStateOf<Boolean>(false) }

    LaunchedEffect(lastError) {
        when (lastError) {
            is ConnectionException -> {
                showServerError = true
            }
            is AuthorizationException -> {
                showAuthError = true
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
            title = stringResource(Res.string.download_error)
        )
    }

    if (showServerError) {
        ToastPopup(
            onDismissRequest = { showServerError = false },
            text = stringResource(Res.string.download_server_error)
        )
    }

    if (showAuthError) {
        ToastPopup(
            onDismissRequest = { showAuthError = false },
            text = stringResource(Res.string.auth_error)
        )
    }
}
