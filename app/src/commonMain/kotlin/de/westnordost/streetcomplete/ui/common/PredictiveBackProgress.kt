package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch

/** Handles back navigation like [NonPredictiveBackHandler], but also returns the progress of a
 *  predictive back gesture, from 0 to 1, so that the UI can follow it.
 *
 *  When the gesture is cancelled, the progress animates back to 0. When it is completed, it stays
 *  where the gesture left off, so that any exit animation can continue from there. */
@Composable
fun rememberPredictiveBackProgress(
    isBackEnabled: Boolean = true,
    onBackCompleted: () -> Unit
): State<Float> {
    val state = rememberNavigationEventState(NavigationEventInfo.None)
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) {
        snapshotFlow { state.transitionState }.collect { transitionState ->
            if (transitionState is NavigationEventTransitionState.InProgress) {
                progress.snapTo(transitionState.latestEvent.progress)
            }
        }
    }
    NavigationBackHandler(
        state = state,
        isBackEnabled = isBackEnabled,
        onBackCancelled = { scope.launch { progress.animateTo(0f) } },
        onBackCompleted = onBackCompleted
    )
    return progress.asState()
}
