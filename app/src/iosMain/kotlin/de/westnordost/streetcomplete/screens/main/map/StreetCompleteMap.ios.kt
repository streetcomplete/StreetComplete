package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Recreates the map presentation after iOS backgrounds its Metal surface.
 *
 * The pinned MapLibre Compose surface only pauses its frame loop on [Lifecycle.Event.ON_STOP].
 * Reattaching the retained logical map creates a fresh render session and replays its style.
 */
@Composable
internal actual fun rememberMapPresentationKey(): Int {
    val lifecycleOwner = LocalLifecycleOwner.current
    var generation by remember(lifecycleOwner) { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        var stopped = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> stopped = true
                Lifecycle.Event.ON_START -> if (stopped) {
                    stopped = false
                    generation += 1
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return generation
}
