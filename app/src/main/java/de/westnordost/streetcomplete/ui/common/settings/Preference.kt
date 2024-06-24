package de.westnordost.streetcomplete.ui.common.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
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
import de.westnordost.streetcomplete.ui.theme.hint
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
            Column() {
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(2f/3f)) {
            Text(name)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.hint
                )
            }
        }
        if (value != null) {
            Row(
                modifier = Modifier.weight(1f/3f),
                horizontalArrangement = Arrangement.End
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    LocalContentColor provides MaterialTheme.colors.hint
                ) {
                    value()
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreferenceCategoryPreview() {
    PreferenceCategory("Preference Category") {}
}

@Preview
@Composable
private fun PreferenceItemPreview() {
    Column {
        Preference(
            name = "Preference",
            onClick = {},
            description = "GNU"
        )
        Preference(
            name = "Preference2",
            onClick = {},
        ) {
            Switch(checked = true, onCheckedChange = {})
            Icon(painterResource(R.drawable.ic_question_24dp), null)
        }
    }
}
