package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.horizontal
import de.westnordost.streetcomplete.ui.ktx.reverse
import de.westnordost.streetcomplete.ui.ktx.toArrangement
import de.westnordost.streetcomplete.ui.ktx.vertical


/**
 * Info button from which an attribution text is expanded towards the start. The attribution text
 * retracts once when the user first starts interacting with the map.
 *
 * @param lastCameraMoveReason The reason reason why the camera moved, last time it moved. See
 *   [CameraState.moveReason].
 * @param attributions List of attributions to show. See
 *   [StyleState.queryAttributionLinks][dev.sargunv.maplibrecompose.compose.StyleState.queryAttributionLinks]
 * @param modifier the Modifier to be applied to this layout node
 * @param textStyle Text style used for the attribution info
 * @param textLinkStyles Text link styles that should be used for the links in the attribution info
 * @param contentAlignment Alignment where the button and attribution texts should be aligned to
 */
@Composable
fun AttributionButton(
    lastCameraMoveReason: CameraMoveReason,
    attributions: List<AttributionLink>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.body2,
    textLinkStyles: TextLinkStyles? = null,
    contentAlignment: Alignment = Alignment.BottomEnd,
) {
    if (attributions.isEmpty()) return

    var expanded by remember { mutableStateOf(true) }

    LaunchedEffect(lastCameraMoveReason) {
        if (lastCameraMoveReason == CameraMoveReason.GESTURE) {
            expanded = false
        }
    }

    val verticalAlignment = remember(contentAlignment) { contentAlignment.vertical }
    val horizontalArrangement =
        remember(contentAlignment) { contentAlignment.horizontal.toArrangement() }

    // rounded corner the size of the info button
    val cornerSize = 20.dp

    // the background is separate from the attribution texts because it should, when visible, also
    // cover the info icon button. This makes the whole setup a bit more complicated, i.e. requires
    // two AnimatedVisibility and the actual content to be wrapped in CompositionLocalProvider for the
    // the content color
    val surfaceColor = MaterialTheme.colors.surface
    val contentColor = contentColorFor(surfaceColor)

    // reverse the layout if necessary: the info button should always stick to the side the whole
    // widget is aligned to
    val dir = LocalLayoutDirection.current
    val rowLayoutDirection = if (horizontalArrangement == Arrangement.End) dir.reverse() else dir

    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalLayoutDirection provides rowLayoutDirection,
    ) {
        Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
            // background for the attribution texts
            AnimatedVisibility(expanded, modifier = Modifier.matchParentSize()) {
                Box(
                    modifier =
                    Modifier.matchParentSize()
                        .padding(4.dp)
                        .background(surfaceColor, RoundedCornerShape(cornerSize))
                )
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InfoIconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(verticalAlignment),
                )
                // attributions texts: after applying the paddings, they should be layout in the normal
                // layout direction again
                AnimatedVisibility(expanded, modifier = Modifier.weight(1f, fill = false)) {
                    // make sure that the text always fits in the rounded corner background
                    Box(Modifier.padding(vertical = 8.dp).padding(end = cornerSize - 4.dp)) {
                        CompositionLocalProvider(LocalLayoutDirection provides dir) {
                            AttributionTexts(
                                attributions = attributions,
                                textStyle = textStyle,
                                textLinkStyles = textLinkStyles,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_info_outline_24dp),
            contentDescription = stringResource(R.string.map_attribution),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttributionTexts(
    attributions: List<AttributionLink>,
    textStyle: TextStyle,
    textLinkStyles: TextLinkStyles?,
    modifier: Modifier = Modifier,
) {
    ProvideTextStyle(textStyle) {
        FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            attributions.forEach {
                Text(
                    buildAnnotatedString {
                        withLink(LinkAnnotation.Url(url = it.url, styles = textLinkStyles)) { append(it.title) }
                    }
                )
            }
        }
    }
}

data class AttributionLink(val title: String, val url: String)


@Immutable
public enum class CameraMoveReason {
    /** The camera hasn't moved yet. */
    NONE,

    /** The camera moved for a reason we don't understand. File a bug report! */
    UNKNOWN,

    /**
     * Camera movement was initiated by the user manipulating the map by panning, zooming, rotating,
     * or tilting.
     */
    GESTURE,

    /** Camera movement was initiated by a call to the public API, or by the compass ornament. */
    PROGRAMMATIC,
}
