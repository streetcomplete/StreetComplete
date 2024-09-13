package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.DialogContentWithIconLayout
import de.westnordost.streetcomplete.screens.user.profile.LaurelWreathBadge
import de.westnordost.streetcomplete.screens.user.profile.getLocalRankCurrentWeekProgress
import de.westnordost.streetcomplete.screens.user.profile.getLocalRankProgress
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.ui.theme.headlineSmall
import java.util.Locale

/** Shows the details for a certain country as a dialog. */
@Composable
fun CountryDialog(
    countryCode: String,
    rank: Int?,
    isCurrentWeek: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // center everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                // dismiss when clicking wherever - no ripple effect
                .clickable(null, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            DialogContentWithIconLayout(
                icon = { Flag(countryCode) },
                content = { isLandscape ->
                    CountryInfoDetails(
                        countryCode = countryCode,
                        rank = rank,
                        isCurrentWeek = isCurrentWeek,
                        isLandscape = isLandscape
                    )
                },
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun CountryInfoDetails(
    countryCode: String,
    rank: Int?,
    isCurrentWeek: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val countryLocale = Locale("", countryCode)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (rank != null) {
            Text(
                text = stringResource(R.string.user_statistics_country_rank2, Locale("", countryCode).displayCountry),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
            )

            val label = stringResource(
                if (isCurrentWeek) R.string.user_profile_current_week_title
                else R.string.user_profile_all_time_title
            )
            val progress =
                if (isCurrentWeek) getLocalRankCurrentWeekProgress(rank)
                else getLocalRankProgress(rank)

            LaurelWreathBadge(
                label = label,
                value = "#$rank",
                progress = progress
            )
        }

        OutlinedButton(
            onClick = {
                val britishCountryName = countryLocale.getDisplayCountry(Locale.UK)
                uriHandler.openUri("https://wiki.openstreetmap.org/wiki/$britishCountryName")
            }
        ) {
            OpenInBrowserIcon()
            Text(
                text = stringResource(R.string.user_statistics_country_wiki_link, countryLocale.displayCountry),
                modifier = Modifier.padding(start = 8.dp),
                textAlign = if (isLandscape) TextAlign.Start else TextAlign.Center
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewCountryDialog() {
    CountryDialog(
        countryCode = "PH",
        rank = 99,
        isCurrentWeek = false,
        onDismissRequest = {}
    )
}
