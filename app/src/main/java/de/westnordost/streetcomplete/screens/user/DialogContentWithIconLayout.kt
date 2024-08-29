package de.westnordost.streetcomplete.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import de.westnordost.streetcomplete.ui.util.backgroundWithPadding

/** Layout that appears like a dialog and leaves an offset for an icon that appears displaced
 *  half-on-top on the dialog. On portrait layout, the icon is on top of the content, on landscape
 *  layout, it is to the start */
@Composable
fun DialogContentWithIconLayout(
    icon: @Composable () -> Unit,
    content: @Composable (isLandscape: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // in landscape layout, dialog would become too tall to fit
    BoxWithConstraints(modifier) {
        val isLandscape = maxWidth > maxHeight

        // scale down icon to fit small devices
        val iconSize = (min(maxWidth, maxHeight) * 0.67f).coerceAtMost(320.dp)

        val backgroundPadding =
            if (isLandscape) PaddingValues(start = iconSize * 0.75f)
            else PaddingValues(top = iconSize * 0.75f)

        val dialogModifier = modifier
            .backgroundWithPadding(
                color = MaterialTheme.colors.surface,
                padding = backgroundPadding,
                shape = MaterialTheme.shapes.medium
            )
            .padding(24.dp)

        val contentColor = contentColorFor(MaterialTheme.colors.surface)
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (isLandscape) {
                Row(
                    modifier = dialogModifier.width(maxWidth.coerceAtMost(720.dp)),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(iconSize),
                        contentAlignment = Alignment.CenterEnd
                    ) { icon() }
                    content(true)
                }
            } else {
                Column(
                    modifier = dialogModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(iconSize),
                        contentAlignment = Alignment.BottomCenter
                    ) { icon() }
                    content(false)
                }
            }
        }
    }
}
