package de.westnordost.streetcomplete.ui.common.feature

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.osm.iconDrawableResource
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.collections.get

/** Icon for a [feature]. Some features, i.e. brand features usually don't have an icon, so a
 *  fallback to the [parentFeature] can be specified. */
@Composable
fun FeatureIcon(
    feature: Feature,
    modifier: Modifier = Modifier,
    parentFeature: Feature? = null,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    val icon = feature.iconDrawableResource
        ?: parentFeature?.iconDrawableResource
        ?: Res.drawable.preset_maki_marker_stroked

    Icon(
        painter = painterResource(icon),
        contentDescription = feature.name,
        modifier = modifier,
        tint = tint,
    )
}
