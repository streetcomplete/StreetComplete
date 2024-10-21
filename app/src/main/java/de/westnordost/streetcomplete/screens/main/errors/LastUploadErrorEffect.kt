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
import de.westnordost.streetcomplete.data.upload.VersionBannedException
import de.westnordost.streetcomplete.util.ktx.toast

/** Depending on the type of error, either display or conditionally offer to report the last
 *  occurred error during upload */
@Composable
fun LastUploadErrorEffect(
    lastError: Exception,
    onReportError: (error: Exception) -> Unit
) {
    val context = LocalContext.current

    var showUploadErrorDialog by remember { mutableStateOf(false) }
    var shownVersionBanned by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lastError) {
        when (lastError) {
            is VersionBannedException -> {
                shownVersionBanned = lastError.banReason
            }
            is ConnectionException -> {
                context.toast(R.string.upload_server_error, Toast.LENGTH_LONG)
            }
            is AuthorizationException -> {
                context.toast(R.string.auth_error, Toast.LENGTH_LONG)
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
            title = stringResource(R.string.upload_error)
        )
    }
}
