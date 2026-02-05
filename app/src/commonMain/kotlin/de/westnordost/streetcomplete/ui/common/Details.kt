package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/** Like the HTML &lt;details&gt; element or a section controlled by
 *  [disclosure controls](https://developer.apple.com/design/human-interface-guidelines/disclosure-controls)
 *  in Apple's human interface guidelines. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Details(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    summary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val alpha = if (enabled) ContentAlpha.high else ContentAlpha.disabled
    Column(modifier) {
        Surface(
            checked = expanded,
            onCheckedChange = onExpandedChange,
            shape = MaterialTheme.shapes.medium,
            enabled = enabled,
        ) {
            ProvideTextStyle(MaterialTheme.typography.button) {
                Row (
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                        contentDescription = null,
                        modifier = Modifier.rotate(if (expanded) 0f else 270f),
                        tint = LocalContentColor.current.copy(alpha = alpha)
                    )
                    Box { summary() }
                }
            }
        }
        AnimatedVisibility(visible = expanded) {
            Box {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun DetailsPreview() {
    val text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam"
    var expanded by rememberSaveable { mutableStateOf(false) }
    Details(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        summary = { Text("Click me!") },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text)
    }
}
