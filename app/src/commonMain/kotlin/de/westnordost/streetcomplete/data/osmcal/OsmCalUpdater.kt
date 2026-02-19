package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.distanceTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
            val nextMonth = Clock.System.now() + 31.days
            val events = apiClient
                .getEvents()
                // only events that start in less than one month. Some local communities add
                // events on OsmCal for the whole year in advance.
                .filter { it.startDate <= nextMonth }
                // only nearby events, less than 25km away as the bird flies (~distance of city
                // center to outer suburbs / about 1 hour drive)
                .filter { it.position.distanceTo(prefs.mapPosition) < 25000 }
            controller.putAll(events)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download the OSM events from OsmCal", e)
        }
    }

    companion object {
        private const val TAG = "WeeklyOsmUpdater"
    }
}
