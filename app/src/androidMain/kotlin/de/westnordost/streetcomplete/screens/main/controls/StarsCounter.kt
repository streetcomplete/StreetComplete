package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CounterWithHalo
import de.westnordost.streetcomplete.ui.common.TextWithHalo
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.theme.titleSmall

/** View that displays the user's quest answer counter */
@Composable
fun StarsCounter(
    count: Int,
    modifier: Modifier = Modifier,
    isCurrentWeek: Boolean = false,
    showProgress: Boolean = false,
) {
    val surfaceColor = MaterialTheme.colors.surface
    val haloColor = LocalElevationOverlay.current?.apply(surfaceColor, 4.dp) ?: surfaceColor

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_star_halo_32dp),
                contentDescription = null,
                tint = haloColor
            )
            Icon(
                painter = painterResource(R.drawable.ic_star_32dp),
                contentDescription = null,
                tint = contentColorFor(surfaceColor)
            )
        }

        if (isCurrentWeek) {
            Column {
                TextWithHalo(
                    text = stringResource(R.string.user_profile_current_week_title),
                    maxLines = 1,
                    haloWidth = 3.dp,
                    elevation = 4.dp,
                    style = MaterialTheme.typography.titleSmall
                )
                CounterWithHalo(
                    count = count,
                    haloWidth = 3.dp,
                    elevation = 4.dp,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        } else {
            CounterWithHalo(
                count = count,
                haloWidth = 3.dp,
                elevation = 4.dp,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewStarsCounter() {
    StarsCounter(
        count = 123,
        isCurrentWeek = true,
        showProgress = true
    )
}
