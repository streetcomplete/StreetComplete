package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.hint
import de.westnordost.streetcomplete.ui.theme.titleSmall

@Composable
fun PreferenceCategoryItem(
    title: String,
    modifier: Modifier = Modifier
) {
    Column {
        Divider()
        Text(
            text = title,
            modifier = modifier.padding(top = 12.dp, start = 16.dp, end = 8.dp, bottom = 8.dp),
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun PreferenceItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: Painter? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.body1)
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.hint
                )
            }
        }
        if (icon != null) {
            Icon(icon, null)
        }
    }
}

@Preview
@Composable
private fun PreferenceCategoryItemPreview() {
    PreferenceCategoryItem("Preference Category")
}

@Preview
@Composable
private fun PreferenceItemPreview() {
    PreferenceItem(
        title = "Preference",
        summary = "GNU",
        icon = painterResource(R.drawable.ic_question_24dp),
        onClick = {}
    )
}
