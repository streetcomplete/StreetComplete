package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.maxheight_sign
import de.westnordost.streetcomplete.resources.maxheight_sign_mutcd
import de.westnordost.streetcomplete.resources.maxheight_sign_yellow
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** A box of fixed size showing a max height sign in the background */
@Composable
fun MaxHeightSign(
    countryCode: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val signBackground = painterResource(getMaxHeightSignDrawable(countryCode))
    CompositionLocalProvider(LocalContentColor provides TrafficSignColor.Black) {
        Box(
            modifier = modifier
                .size(256.dp)
                .drawBehind { with(signBackground) { draw(size) } }
                .padding(48.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

// source: https://commons.wikimedia.org/wiki/Category:SVG_prohibitory_road_signs_%E2%80%93_height_limit
private fun getMaxHeightSignDrawable(countryCode: String?): DrawableResource = when (countryCode) {
    "FI", "IS", "SE", "NG", -> Res.drawable.maxheight_sign_yellow
    "AU", "CA", "US", ->  Res.drawable.maxheight_sign_mutcd
    else -> Res.drawable.maxheight_sign
}
