package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.amaanprobably.scrollbar.ScrollbarDefaults
import io.github.amaanprobably.scrollbar.ThumbAppearance
import io.github.amaanprobably.scrollbar.scrollbar

// TODO Compose: replace with foundation scrollbar once/if they become available
@Composable
fun Modifier.scrollbar(
    listState: LazyListState,
    contentPadding: PaddingValues = PaddingValues.Zero,
): Modifier {
    val thumbColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val pressedThumbColor = MaterialTheme.colors.secondary
    val style = remember(thumbColor, pressedThumbColor) {
        // The thin bar on every platform (already the default on iOS and desktop),
        // with each platform's own sizes.
        ScrollbarDefaults.style(
            thumbAppearance = ThumbAppearance.Bar,
            thumbColor = thumbColor,
            pressedThumbColor = pressedThumbColor,
        )
    }
    return scrollbar(listState, contentPadding = contentPadding, style = style)
}
