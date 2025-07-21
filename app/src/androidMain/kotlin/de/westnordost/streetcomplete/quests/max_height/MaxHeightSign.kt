package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.maxheight_sign
import de.westnordost.streetcomplete.resources.maxheight_sign_mutcd
import de.westnordost.streetcomplete.resources.maxheight_sign_yellow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** A box of fixed size showing a max height sign in the background */
@Composable
fun MaxHeightSign(
    countryCode: String?,
    content: @Composable BoxScope.() -> Unit,
) {
    val signBackground = painterResource(getMaxHeightSignDrawable(countryCode))
    Box(
        modifier = Modifier
            .size(256.dp)
            .drawBehind { with(signBackground) { draw(size) } }
            .padding(48.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun getMaxHeightSignDrawable(countryCode: String?): DrawableResource =
    when (countryCode) {
        "FI", "IS", "SE", "NG", -> {
            Res.drawable.maxheight_sign_yellow
        }
        // source: https://commons.wikimedia.org/wiki/File:Road_Warning_signs_around_the_World.svg
        "AR", "AU", "BR", "BZ", "CA", "CL", "CO", "CR", "DO", "EC", "GT", "GY", "HN",
        "ID", "IE", "JM", "JP", "LK", "LR", "MM", "MX", "MY", "NI", "NZ", "PA", "PE",
        "PG","SV", "TH", "TL", "US", "UY", "VE", -> {
            Res.drawable.maxheight_sign_mutcd
        }
        else -> {
            Res.drawable.maxheight_sign
        }
    }
