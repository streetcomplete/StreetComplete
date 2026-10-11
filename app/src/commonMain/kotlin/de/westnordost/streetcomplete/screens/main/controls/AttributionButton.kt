package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_info_outline_24
import de.westnordost.streetcomplete.resources.map_attribution
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.overlay.AttributionLinks
import org.maplibre.compose.overlay.AttributionStyle
import org.maplibre.compose.overlay.ExpandingAttributionButton

/** Info button from which the map attribution is expanded. It collapses when the user interacts
 *  with the map. This is [ExpandingAttributionButton] with the colors, typography and widgets of
 *  the Material theme. */
@Composable
fun AttributionButton(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.BottomStart,
) {
    val color = MaterialTheme.colors.surface
    val contentColor = contentColorFor(color)
    val textStyle = MaterialTheme.typography.body2.copy(color = contentColor)
    ExpandingAttributionButton(
        modifier = modifier,
        contentAlignment = contentAlignment,
        toggleButton = { onClick ->
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_info_outline_24),
                    contentDescription = stringResource(Res.string.map_attribution),
                )
            }
        },
        expandedContent = { attributions, style ->
            AttributionLinks(
                attributions,
                textStyle = style,
                linkStyles = TextLinkStyles(),
                breakWithinAttribution = true,
            )
        },
        expandedStyle = AttributionStyle(
            containerColor = color,
            contentColor = contentColor,
            textStyle = textStyle,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(24.dp),
        ),
        collapsedStyle = AttributionStyle(
            containerColor = color.copy(alpha = 0f),
            contentColor = contentColor,
            textStyle = textStyle,
        ),
    )
}
