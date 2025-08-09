package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import org.jetbrains.compose.resources.painterResource

/** Like the HTML &lt;details&gt; element or a section controlled by
 *  [disclosure controls](https://developer.apple.com/design/human-interface-guidelines/disclosure-controls)
 *  in Apple's human interface guidelines. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Details(
    isInitiallyExpanded: Boolean,
    summary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(isInitiallyExpanded) }
    Column(modifier) {
        Surface(
            checked = expanded,
            onCheckedChange = { expanded = it },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
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
                        modifier = Modifier.rotate(if (expanded) 0f else 270f)
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

@Preview @Composable
private fun DetailsPreview() {
    val text = LoremIpsum(10).values.joinToString(" ")
    Details(
        isInitiallyExpanded = false,
        summary = { Text("Click me!") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
