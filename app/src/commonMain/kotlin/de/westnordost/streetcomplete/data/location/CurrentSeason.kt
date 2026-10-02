package de.westnordost.streetcomplete.data.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Month
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.number
import org.maplibre.compose.location.LocationProvider
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.maplibre.compose.location.LocationEvent

/**
 * Provides the season in the users hemisphere and timezone.
 * The implementation is split into multiple functions to allow for easier testing.
 * The season is updated every time a new location is added.
 * */
class CurrentSeason(
    private val locationProvider: LocationProvider,
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("SurveyChecker"))

    init {
        coroutineScope.launch {
            locationProvider.updates().collect { locationEvent ->
                if (locationEvent is LocationEvent.Update) {
                    addRecentLocation(locationEvent.measurement.toLocation())
                }
            }
        }
    }

    var season: String = "unknown"

    fun addRecentLocation(location: Location) {
        val southernHemisphere = isSouthernHemisphere(location)
        updateSeason(southernHemisphere, Clock.System.todayIn(TimeZone.currentSystemDefault()).month)
    }

    fun getCurrentSeason() : String
    {
        return season
    }

    fun updateSeason(southernHemisphere: Boolean, month: Month) {
        season = when (month.number) {
            3, 4, 5 -> if (southernHemisphere) "autumn" else "spring"
            6, 7, 8 -> if (southernHemisphere) "winter" else "summer"
            9, 10, 11 -> if (southernHemisphere) "spring" else "autumn"
            else -> if (southernHemisphere) "summer" else "winter" // 12, 1, 2
        }
    }

    fun isSouthernHemisphere(location: Location): Boolean{
        var southernHemisphere = false
        if (location.position.latitude < 0) {
            southernHemisphere = true
        }
        return southernHemisphere
    }
}
