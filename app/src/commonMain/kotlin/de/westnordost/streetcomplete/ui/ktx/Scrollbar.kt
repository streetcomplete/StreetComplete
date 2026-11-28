package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.amaanprobably.scrollbar.ScrollbarDefaults
import io.github.amaanprobably.scrollbar.ScrollbarStyle
import io.github.amaanprobably.scrollbar.ThumbAppearance
import io.github.amaanprobably.scrollbar.scrollbar

// TODO Compose: replace with foundation scrollbar once/if they become available
@Composable
fun Modifier.scrollbar(
    listState: LazyListState,
    contentPadding: PaddingValues = PaddingValues.Zero,
): Modifier =
    scrollbar(listState, contentPadding = contentPadding, style = defaultScrollbarStyle())

@Composable
fun Modifier.scrollbar(
    scrollState: ScrollState,
    contentPadding: PaddingValues = PaddingValues.Zero,
): Modifier =
    scrollbar(scrollState, contentPadding = contentPadding, style = defaultScrollbarStyle())

@Composable
private fun defaultScrollbarStyle(): ScrollbarStyle {
    val thumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val pressedThumbColor = MaterialTheme.colors.secondary
    return remember(thumbColor, pressedThumbColor) {
        // The thin bar on every platform (already the default on iOS and desktop),
        // with each platform's own sizes.
        ScrollbarDefaults.style(
            thumbAppearance = ThumbAppearance.Bar,
            thumbColor = thumbColor,
            pressedThumbColor = pressedThumbColor,
        )
    }
}
