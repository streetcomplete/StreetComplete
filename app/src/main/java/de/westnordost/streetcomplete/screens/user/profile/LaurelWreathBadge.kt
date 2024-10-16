package de.westnordost.streetcomplete.screens.user.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.ui.ktx.toDp
import de.westnordost.streetcomplete.ui.theme.GrassGray
import de.westnordost.streetcomplete.ui.theme.GrassGreen
import de.westnordost.streetcomplete.ui.theme.White
import de.westnordost.streetcomplete.ui.theme.titleLarge

@Composable
fun LaurelWreathBadge(
    label: String,
    value: String,
    progress: Float,
    modifier: Modifier = Modifier,
    animationDuration: Int = 2000,
    animationDelay: Int = 0,
    startBackgroundColor: Color = GrassGray,
    finalBackgroundColor: Color = GrassGreen,
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val animation = remember { Animatable(0f) }
        LaunchedEffect(progress) {
            animation.animateTo(progress, tween(
                durationMillis = animationDuration,
                delayMillis = animationDelay)
            )
        }

        Box(
            modifier = Modifier
                .size(128.sp.toDp()) // scale size with font scaling
                .drawBehind {
                    drawCircle(color = startBackgroundColor)
                    drawCircle(color = finalBackgroundColor, alpha = animation.value)
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            LaurelWreath(progress = animation.value)
            Text(
                text = value,
                color = White,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun BadgePreview() {
    LaurelWreathBadge(label = "Label\ntext", value = "#12", progress = 1.0f)
}
