package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.upload.VersionBannedException
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ToastPopup
import org.jetbrains.compose.resources.stringResource

/** Depending on the type of error, either display or conditionally offer to report the last
 *  occurred error during upload */
@Composable
fun LastUploadErrorEffect(
    lastError: Exception,
    onReportError: (error: Exception) -> Unit
) {
    var showUploadErrorDialog by remember { mutableStateOf(false) }
    var shownVersionBanned by remember { mutableStateOf<String?>(null) }
    var showServerError by remember { mutableStateOf<Boolean>(false) }
    var showAuthError by remember { mutableStateOf<Boolean>(false) }

    LaunchedEffect(lastError) {
        when (lastError) {
            is VersionBannedException -> {
                shownVersionBanned = lastError.banReason
            }
            is ConnectionException -> {
                showServerError = true
            }
            is AuthorizationException -> {
                showAuthError = true
            }
            else -> {
                showUploadErrorDialog = true
            }
        }
    }

    if (shownVersionBanned != null) {
        VersionBannedDialog(
            onDismissRequest = { shownVersionBanned = null },
            reason = shownVersionBanned
        )
    }

    if (showUploadErrorDialog) {
        SendErrorReportDialog(
            onDismissRequest = { showUploadErrorDialog = false },
            onConfirmed = { onReportError(lastError) },
            title = stringResource(Res.string.upload_error)
        )
    }

    if (showServerError) {
        ToastPopup(
            onDismissRequest = { showServerError = false },
            text = stringResource(Res.string.upload_server_error)
        )
    }

    if (showAuthError) {
        ToastPopup(
            onDismissRequest = { showAuthError = false },
            text = stringResource(Res.string.auth_error)
        )
    }
}
