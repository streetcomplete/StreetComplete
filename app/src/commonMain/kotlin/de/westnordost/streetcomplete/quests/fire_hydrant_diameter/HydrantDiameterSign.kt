package de.westnordost.streetcomplete.quests.fire_hydrant_diameter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_de
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_fi
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_gb
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_hu
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_nl
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_pl
import de.westnordost.streetcomplete.resources.fire_hydrant_sign_ua
import de.westnordost.streetcomplete.ui.common.AbsoluteBox
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor.White
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor.Blue
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor.Black
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/** A box of fixed size showing a hydrant diameter sign in the background, places its content at the
 *  correct position */
@Composable
fun HydrantDiameterSign(
    countryCode: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val sign = getHydrantDiameterSign(countryCode)
    CompositionLocalProvider(LocalContentColor provides sign.contentColor) {
        AbsoluteBox(modifier) {
            Image(painterResource(sign.background), null)
            Box(
                modifier = Modifier
                    .absoluteOffset(x = sign.contentOffset.x, y = sign.contentOffset.y)
                    .size(sign.contentSize),
                contentAlignment = Alignment.Center,
                content = content
            )
        }
    }
}

// main source: https://commons.wikimedia.org/wiki/Category:Fire_hydrant_signs_by_country
private fun getHydrantDiameterSign(countryCode: String?): FireHydrantDiameterSign =
    when (countryCode) {
        "DE", "AT", "BE", "LU",  -> FireHydrantDiameterSign.De
        "GB", "IE" -> FireHydrantDiameterSign.Gb
        "FI" -> FireHydrantDiameterSign.Fi
        "HU" -> FireHydrantDiameterSign.Hu
        "NL" -> FireHydrantDiameterSign.Nl
        "PL" -> FireHydrantDiameterSign.Pl
        "UA" -> FireHydrantDiameterSign.Ua
        else -> FireHydrantDiameterSign.De // should never happen, but let's not crash in that case
    }

private enum class FireHydrantDiameterSign(
    val background: DrawableResource,
    val contentColor: Color,
    val contentOffset: DpOffset,
    val contentSize: DpSize,
) {
    De(Res.drawable.fire_hydrant_sign_de, Black, DpOffset(120.dp, 20.dp), DpSize(148.dp, 88.dp)),
    Fi(Res.drawable.fire_hydrant_sign_fi, Black, DpOffset(84.dp, 20.dp), DpSize(152.dp, 88.dp)),
    Gb(Res.drawable.fire_hydrant_sign_gb, Black, DpOffset(84.dp, 40.dp), DpSize(152.dp, 88.dp)),
    Hu(Res.drawable.fire_hydrant_sign_hu, Black, DpOffset(120.dp, 20.dp), DpSize(148.dp, 88.dp)),
    Nl(Res.drawable.fire_hydrant_sign_nl, White, DpOffset(92.dp, 132.dp), DpSize(176.dp, 88.dp)),
    Pl(Res.drawable.fire_hydrant_sign_pl, White, DpOffset(128.dp, 24.dp), DpSize(160.dp, 88.dp)),
    Ua(Res.drawable.fire_hydrant_sign_ua, Blue, DpOffset(120.dp, 144.dp), DpSize(138.dp, 88.dp)),
}


@Preview
@Composable
private fun HydrantDiameterSignFormPreview() {
    HydrantDiameterSign("UA") { Box(Modifier.fillMaxSize().background(Color.Gray)) }
}
