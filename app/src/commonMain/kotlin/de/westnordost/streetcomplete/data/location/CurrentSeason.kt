package de.westnordost.streetcomplete.data.location

import kotlinx.datetime.Month
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.number

/** Saves the hemisphere the user is in. */
object CurrentSeason {

    var season: String = "unknown"

    fun addRecentLocation(location: Location) {
        var southernHemisphere = isSouthernHemisphere(location)

        updateSeason(southernHemisphere, Clock.System.todayIn(TimeZone.currentSystemDefault()).month)
    }

    val getCurrentSeason: String
        get() {
            return season
        }

    fun updateSeason(southernHemisphere: Boolean, month: Month): String {
        season = when (month.number) {
            3, 4, 5 -> if (southernHemisphere) "autumn" else "spring"
            6, 7, 8 -> if (southernHemisphere) "winter" else "summer"
            9, 10, 11 -> if (southernHemisphere) "spring" else "autumn"
            else -> if (southernHemisphere) "summer" else "winter" // 12, 1, 2
        }
        return season
    }

    fun isSouthernHemisphere(location: Location): Boolean{
        var southernHemisphere = false
        if (location.position.latitude < 0) {
            southernHemisphere = true
        }
        return southernHemisphere
    }
}
