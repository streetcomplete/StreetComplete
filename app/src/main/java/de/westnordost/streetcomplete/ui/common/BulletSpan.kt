package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BulletSpan(
    modifier: Modifier = Modifier,
    bullet: String = "•",
    content: @Composable (() -> Unit),
) {
    Row(modifier = modifier) {
        Text(bullet, modifier = Modifier.padding(horizontal = 8.dp))
        content()
    }
}
