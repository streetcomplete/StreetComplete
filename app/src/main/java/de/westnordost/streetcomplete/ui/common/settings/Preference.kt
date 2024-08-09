package de.westnordost.streetcomplete.ui.common.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.titleSmall

@Composable
fun PreferenceCategory(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Divider()
        if (title != null) {
            Text(
                text = title,
                modifier = modifier.padding(top = 12.dp, start = 16.dp, end = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.titleSmall
            )
        }
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.body1) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun Preference(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    value: @Composable (RowScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = 64.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            space = 0.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(2 / 3f)
            )
            if (value != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    LocalContentAlpha provides ContentAlpha.medium
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1 / 3f)
                    ) { value() }
                }
            }
        }
        if (description != null) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.body2,
                LocalContentAlpha provides ContentAlpha.medium
            ) {
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreferencePreview() {
    PreferenceCategory("Preference Category") {
        Preference(
            name = "Preference",
            onClick = {},
        )
        Preference(
            name = "Preference with switch",
            onClick = {}
        ) {
            Switch(checked = true, onCheckedChange = {})
        }
        Preference(
            name = "Preference",
            onClick = {},
            description = "A long description which may actually be several lines long, so it should wrap."
        ) {
            Icon(painterResource(R.drawable.ic_chevron_next_24dp), null)
        }

        Preference(
            name = "Long preference name that wraps",
            onClick = {},
        ) {
            Text("Long preference value")
        }
    }
}
