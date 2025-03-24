package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.controls.ktx.Reverse

/**
 * Info button from which an attribution popup text is expanded from. The attribution text retracts
 * once when the user first starts interacting with the map.
 *
 * @param userHasMovedMap The user has moved the map.
 * @param attributions List of attributions to show.
 * @param modifier the Modifier to be applied to this layout node
 * @param textStyle Text style used for the attribution info
 * @param textLinkStyles Text link styles that should be used for the links in the attribution info
 */
@Composable
public fun AttributionButton(
    userHasMovedMap: Boolean,
    attributions: List<AttributionLink>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.body2,
    textLinkStyles: TextLinkStyles? = null,
) {
    if (attributions.isEmpty()) return

    val expanded = remember { MutableTransitionState(true) }

    // close popup on moving the map
    LaunchedEffect(userHasMovedMap) {
        if (userHasMovedMap) expanded.targetState = false
    }

    Box(modifier) {
        IconButton(onClick = { expanded.targetState = !expanded.currentState }) {
            InfoIcon()
        }
        if (expanded.currentState || expanded.targetState) {
            // popup position provider places popup superimposed over the icon button, the arrangement
            // and alignment of its content depends on the position of the icon button in relation to the
            // screen.
            var alignLeft by remember { mutableStateOf(false) }
            var alignTop by remember { mutableStateOf(false) }
            val popupPositionProvider = SuperimposingPopupPositionProvider { left, top ->
                alignLeft = left
                alignTop = top
            }
            val verticalAlignment = if (alignTop) Alignment.Top else Alignment.Bottom
            val horizontalArrangement =
                if (alignLeft) Arrangement.Absolute.Left else Arrangement.Absolute.Reverse

            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { expanded.targetState = false },
            ) {
                AnimatedVisibility(visibleState = expanded, enter = fadeIn(), exit = fadeOut()) {
                    Surface(shape = RoundedCornerShape(24.dp), elevation = 8.dp) {
                        // the content of the popup should be aligned centered vertically in general, only the
                        // icon button should be in the corner, so that it exactly overlaps the original button
                        Row(
                            horizontalArrangement = horizontalArrangement,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { expanded.targetState = false },
                                modifier = Modifier.align(verticalAlignment),
                            ) {
                                InfoIcon()
                            }
                            AttributionTexts(
                                attributions = attributions,
                                textStyle = textStyle,
                                textLinkStyles = textLinkStyles,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            // icon buttons are automatically padded to have a certain click size, which makes the
                            // popup appear misaligned if we don't also add some extra padding on the other side
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_info_outline_24dp),
        contentDescription = stringResource(R.string.map_attribution),
        modifier = modifier,
    )
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
        FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            attributions.forEach {
                val attributionString = buildAnnotatedString {
                    val link = LinkAnnotation.Url(url = it.url, styles = textLinkStyles)
                    withLink(link) { append(it.title) }
                }
                Text(attributionString)
            }
        }
    }
}

data class AttributionLink(val title: String, val url: String)
