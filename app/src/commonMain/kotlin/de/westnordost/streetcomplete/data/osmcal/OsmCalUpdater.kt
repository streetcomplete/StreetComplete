package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.ApplicationConstants.CALENDAR_EVENT_MAX_DISTANCE
import de.westnordost.streetcomplete.ApplicationConstants.CALENDAR_EVENT_MAX_IN_ADVANCE_NOTIFICATION
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.distanceTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** Updates local OSM events */
class OsmCalUpdater(
    private val apiClient: OsmCalApiClient,
    private val controller: CalendarEventsController,
    private val prefs: Preferences
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun update() = coroutineScope.launch(Dispatchers.IO) {
        try {
            val nextMonth = Clock.System.now() + CALENDAR_EVENT_MAX_IN_ADVANCE_NOTIFICATION
            val events = apiClient
                .getEvents()
                // only events that start in less than one month. Some local communities add
                // events on OsmCal for the whole year in advance.
                .filter { it.startDate <= nextMonth }
                // only nearby events
                .filter { it.position.distanceTo(prefs.mapPosition) < CALENDAR_EVENT_MAX_DISTANCE }
            controller.putAll(events)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download the OSM events from OsmCal", e)
        }
    }

    companion object {
        private const val TAG = "WeeklyOsmUpdater"
    }
}
