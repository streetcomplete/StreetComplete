package de.westnordost.streetcomplete.screens.user.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.DialogContentWithIconLayout
import de.westnordost.streetcomplete.ui.common.OpenInBrowserIcon
import de.westnordost.streetcomplete.util.ktx.openUri
import java.util.Locale

@Composable
fun CountryInfoDialog(
    countryCode: String,
    count: Int,
    rank: Int?,
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
                .clickable(remember { MutableInteractionSource() }, null) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            DialogContentWithIconLayout(
                icon = {
                    Image(flagPainterResource(countryCode), null, Modifier.fillMaxSize())
                },
                content = { isLandscape ->
                    CountryInfoDetails(
                        countryCode = countryCode,
                        count = count,
                        rank = rank,
                        isLandscape = isLandscape
                    )
                },
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun flagPainterResource(countryCode: String): Painter {
    val context = LocalContext.current
    val lowerCaseCountryCode = countryCode.lowercase().replace('-', '_')
    val id = context.resources.getIdentifier("ic_flag_$lowerCaseCountryCode", "drawable", context.packageName)
    return painterResource(id)
}

@Composable
private fun CountryInfoDetails(
    countryCode: String,
    count: Int,
    rank: Int?,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val countryLocale = Locale("", countryCode)

    Column(
        modifier = modifier,
        horizontalAlignment = if (isLandscape) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatingBigStarCount(totalCount = count)

        if (rank != null && rank < 500 && count > 50) {
            Text(stringResource(R.string.user_statistics_country_rank, rank, countryLocale.displayCountry))
        }

        OutlinedButton(
            onClick = {
                val britishCountryName = countryLocale.getDisplayCountry(Locale.UK)
                context.openUri("https://wiki.openstreetmap.org/wiki/$britishCountryName")
            }
        ) {
            OpenInBrowserIcon()
            Text(
                text = stringResource(R.string.user_statistics_country_wiki_link, countryLocale.displayCountry),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(device = Devices.NEXUS_5) // darn small device
@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewCountryInfoDialog() {
    CountryInfoDialog(
        countryCode = "PH",
        count = 999,
        rank = 99,
        onDismissRequest = {}
    )
}
