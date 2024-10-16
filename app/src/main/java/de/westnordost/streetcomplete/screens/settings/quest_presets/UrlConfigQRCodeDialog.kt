package de.westnordost.streetcomplete.screens.settings.quest_presets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CopyIcon
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.util.ktx.toast
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun UrlConfigQRCodeDialog(
    onDismissRequest: () -> Unit,
    url: String,
) {
    val clipboardManager = LocalClipboardManager.current

    val qrCode = rememberQrCodePainter(url)

    InfoDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.quest_presets_share)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.urlconfig_qr_code_description))
                Image(
                    painter = qrCode,
                    contentDescription = url,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
                val context = LocalContext.current
                OutlinedTextField(
                    value = TextFieldValue(url, selection = TextRange(0, url.length)),
                    onValueChange = { /* the text is not changed */ },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.urlconfig_as_url)) },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(url))
                            // TODO Compose: Need a multiplatform solution for toasts. Either
                            //  something with Snackbar Host, a third party library like
                            //  https://github.com/dokar3/compose-sonner or self-made, like e.g.
                            //  https://github.com/T8RIN/ComposeToast/blob/main/ToastHost.kt
                            context.toast(R.string.urlconfig_url_copied)
                        }) {
                            CopyIcon()
                        }
                    },
                    readOnly = true,
                    singleLine = true,
                )
            }
        }
    )
}

@PreviewLightDark // QR code should be on white background, otherwise bad (-:
@Composable
private fun PreviewUrlConfigQRDialog() {
    AppTheme {
        UrlConfigQRCodeDialog(
            url = "https://streetcomplete.app/s?q=lesykuk5tsr032wpat165slc0zx1ns7i7",
            onDismissRequest = {}
        )
    }
}
