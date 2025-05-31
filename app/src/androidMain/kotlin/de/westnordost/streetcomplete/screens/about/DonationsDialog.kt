package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.donation_github
import de.westnordost.streetcomplete.resources.donation_liberapay
import de.westnordost.streetcomplete.resources.donation_patreon
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.painterResource

@Composable
fun DonationsDialog(
    onDismissRequest: () -> Unit,
    onClickLink: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { /* no buttons, click outside to close */ },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.about_description_donate))
                DonationPlatformItems(onClickLink, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

@Composable
fun DonationsGooglePlayDialog(onDismissRequest: () -> Unit) {
    InfoDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(stringResource(R.string.about_description_donate_google_play3)) }
    )
}

@Composable
fun DonationPlatformItems(
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DonationPlatformItem(
            title = "GitHub Sponsors",
            painter = painterResource(Res.drawable.donation_github),
            url = "https://github.com/sponsors/westnordost",
            onClickLink
        )
        DonationPlatformItem(
            title = "Liberapay",
            painter = painterResource(Res.drawable.donation_liberapay),
            url = "https://liberapay.com/westnordost",
            onClickLink
        )
        DonationPlatformItem(
            title = "Patreon",
            painter = painterResource(Res.drawable.donation_patreon),
            url = "https://patreon.com/westnordost",
            onClickLink
        )
    }
}

@Composable
fun DonationPlatformItem(
    title: String,
    painter: Painter,
    url: String,
    onClickLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickLink(url) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter, null)
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview
@Composable
private fun DonationsDialogPreview() {
    DonationsDialog({}, {})
}

@Preview
@Composable
private fun DonationsGooglePlayDialogPreview() {
    DonationsGooglePlayDialog({})
}
