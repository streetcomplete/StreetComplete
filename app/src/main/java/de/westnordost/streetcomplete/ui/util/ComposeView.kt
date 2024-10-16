package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import de.westnordost.streetcomplete.ui.theme.AppTheme

fun ComposeView.content(content: @Composable () -> Unit) {
    setContent { AppTheme { content() } }
}
