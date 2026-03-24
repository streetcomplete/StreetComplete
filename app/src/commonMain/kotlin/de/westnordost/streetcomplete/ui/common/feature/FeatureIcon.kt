package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import de.westnordost.streetcomplete.resources.preset_maki_marker_stroked
import org.jetbrains.compose.resources.painterResource

/** Icon for a [feature]. Some features, i.e. brand features usually don't have an icon, so a
 *  fallback to the [parentFeature] can be specified. */
@Composable
fun FeatureIcon(
    feature: Feature,
    modifier: Modifier = Modifier,
    parentFeature: Feature? = null,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    // brand features usually don't have an own icon, so, we fall back to parent feature, e.g. for
    // Aldi, use icon of shop/supermarket. Finally, if there is no icon at all, use a
    // placeholder
    val iconResourceName = feature.iconResourceName ?: parentFeature?.iconResourceName
    val icon = Res.allDrawableResources[iconResourceName] ?: Res.drawable.preset_maki_marker_stroked

    Icon(
        painter = painterResource(icon),
        contentDescription = iconResourceName,
        modifier = modifier,
        tint = tint,
    )
}

private val Feature.iconResourceName: String? get() =
    icon?.let { "preset_" + it.replace('-', '_') }
