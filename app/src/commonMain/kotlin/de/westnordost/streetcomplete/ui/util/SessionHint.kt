package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/** Whether to show the hint identified by [key] in this composition, once per app process.
 *  The hint is consumed on entering an enabled composition, even if it is dismissed before showing.
 *  The decision is remembered so multiple elements can share it. */
@Composable
fun rememberSessionHint(key: String, enabled: Boolean = true): Boolean {
    val showHint = remember(key, enabled) { enabled && key !in shownHints }
    LaunchedEffect(key, enabled) {
        if (showHint) shownHints.add(key)
    }
    return showHint
}

private val shownHints = mutableSetOf<String>()
