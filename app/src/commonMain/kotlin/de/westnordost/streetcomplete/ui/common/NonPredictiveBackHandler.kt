package de.westnordost.streetcomplete.ui.common

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/** Handles back navigation by only reacting to a completed back gesture, i.e. without providing
 *  any [NavigationEventInfo] or observing the progress of a predictive back gesture.
 *
 *  Like [NavigationBackHandler], this should be called unconditionally and be enabled or disabled
 *  via [isBackEnabled]. */
@Composable
fun NonPredictiveBackHandler(
    isBackEnabled: Boolean = true,
    onBackCompleted: () -> Unit
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = isBackEnabled,
        onBackCompleted = onBackCompleted
    )
}
