package de.westnordost.streetcomplete.screens.user.edits

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.flags.FlagAlignment
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import de.westnordost.streetcomplete.ui.ktx.innerBorder
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Flag image with a thin border around it so that a white flag color can be distinguished from the
 *  background */
@Composable
fun Flag(
    countryCode: String,
    modifier: Modifier = Modifier,
) {
    val resource = Res.getFlag(countryCode) ?: return
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    Image(
        painter = painterResource(resource),
        contentDescription = countryCode,
        modifier = modifier.innerBorder(1.dp, color)
    )
}

/** Circular flag image with a thin border around it so that a white flag color can be distinguished
 *  from the background */
@Composable
fun CircularFlag(
    countryCode: String,
    modifier: Modifier = Modifier,
    flagAlignment: FlagAlignment = FlagAlignment.Center
) {
    val resource = Res.getFlag(countryCode) ?: return
    val painter = painterResource(resource)
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)

    Image(
        painter = painter,
        contentDescription = countryCode,
        alignment = flagAlignment.alignment,
        contentScale = flagAlignment.contentScale,
        modifier = modifier
            .size(painter.intrinsicSize.minDimension.toInt().pxToDp())
            .innerBorder(1.dp, color, CircleShape)
            .clip(CircleShape)
    )
}

private fun Res.getFlag(countryCode: String): DrawableResource? =
    allDrawableResources["flag_$countryCode".lowercase()]

private val FlagAlignment.alignment: Alignment get() = when (this) {
    FlagAlignment.Left ->        AbsoluteAlignment.CenterLeft
    FlagAlignment.CenterLeft ->  BiasAbsoluteAlignment(-0.5f, 0f)
    FlagAlignment.Center ->      Alignment.Center
    FlagAlignment.CenterRight -> BiasAbsoluteAlignment(+0.5f, 0f)
    FlagAlignment.Right ->       AbsoluteAlignment.CenterRight
    FlagAlignment.Stretch ->     Alignment.Center
}

private val FlagAlignment.contentScale: ContentScale get() = when (this) {
    FlagAlignment.Stretch -> ContentScale.FillBounds
    else -> ContentScale.Crop
}
