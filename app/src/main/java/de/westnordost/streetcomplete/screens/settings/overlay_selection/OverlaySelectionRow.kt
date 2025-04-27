package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** Single item in the overlay selection list. Shows icon + title, whether it is enabled and
 *  whether it is disabled by default */
@Composable
fun OverlaySelectionRow(
    item: OverlaySelection,
    onToggleSelection: (isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (!item.selected) ContentAlpha.disabled else ContentAlpha.high

    Row(
        modifier = modifier.height(IntrinsicSize.Min).padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(item.overlay.icon),
            contentDescription = item.overlay.name,
            modifier = Modifier.size(48.dp).alpha(alpha),
        )
        Column(
            modifier = Modifier.padding(start = 16.dp).weight(0.1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(item.overlay.title),
                modifier = Modifier.alpha(alpha),
                style = MaterialTheme.typography.body1,
            )
            if (item.overlay.defaultDisabledMessage != 0) {
                DisabledHint(stringResource(R.string.questList_disabled_by_default))
            }
        }
        Box(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = item.selected,
                onCheckedChange = onToggleSelection
            )
        }
    }
}

@Composable
private fun DisabledHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        fontStyle = FontStyle.Italic,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
    )
}
