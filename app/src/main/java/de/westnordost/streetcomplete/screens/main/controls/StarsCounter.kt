package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.Counter

/** View that displays the user's quest answer counter */
@Composable
fun StarsCounter(
    count: Int,
    modifier: Modifier = Modifier,
    isCurrentWeek: Boolean = false,
    showProgress: Boolean = false,
) {
    val textShadow = Shadow(offset = Offset(0f, 1f), blurRadius = 4f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(42.dp),
                    color = MaterialTheme.colors.secondary
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_star_white_shadow_32dp),
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
        }
        Column {
            AnimatedVisibility (isCurrentWeek) {
                Text(
                    text = stringResource(R.string.user_profile_current_week_title),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    style = MaterialTheme.typography.body2.copy(shadow = textShadow),
                )
            }
            Counter(
                count = count,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.body1.copy(shadow = textShadow),
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
