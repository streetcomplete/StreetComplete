package de.westnordost.streetcomplete.screens.user.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.Counter
import de.westnordost.streetcomplete.ui.theme.headlineLarge
import kotlin.math.min
import kotlin.math.pow

/** A large star counter that animates the count from 0 to [totalCount]. The animation can be
 *  made to finish immediately by tapping on it. */
@Composable
fun AnimatingBigStarCount(
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    var showFinalCount by remember { mutableStateOf(false) }
    val scale = remember(totalCount) { 0.4f + min(totalCount / 100f, 1f) * 0.6f }
    val countAnim = remember(totalCount) { Animatable(0f) }
    val displayedCount = if (showFinalCount) totalCount else (totalCount * countAnim.value).toInt()

    LaunchedEffect(totalCount) {
        val duration = (300 + (totalCount * 500.0).pow(0.6)).toInt()
        countAnim.animateTo(1f, tween(duration))
    }

    Row(
        modifier = modifier
            .scale(scale)
            .clickable { showFinalCount = true },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_star_48dp), null)
        Counter(
            count = displayedCount,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}
