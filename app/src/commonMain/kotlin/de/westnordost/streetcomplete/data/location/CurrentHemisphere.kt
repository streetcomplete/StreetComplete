package de.westnordost.streetcomplete.data.location

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.number

/** Saves the hemisphere the user is in. */
object CurrentHemisphere {
    enum class Hemisphere {
        NORTH, SOUTH
    }

    var hemisphere: Hemisphere = Hemisphere.NORTH
        private set

    fun addRecentLocation(location: Location) {
        hemisphere = if (location.position.latitude >= 0) {
            Hemisphere.NORTH
        } else {
            Hemisphere.SOUTH
        }
    }

    val currentSeason: String
        get() {
            val month = Clock.System.todayIn(TimeZone.currentSystemDefault()).month
            return when (month.number) {
                3, 4, 5 -> if (hemisphere == Hemisphere.SOUTH) "autumn" else "spring"
                6, 7, 8 -> if (hemisphere == Hemisphere.SOUTH) "winter" else "summer"
                9, 10, 11 -> if (hemisphere == Hemisphere.SOUTH) "spring" else "autumn"
                else -> if (hemisphere == Hemisphere.SOUTH) "summer" else "winter" // 12, 1, 2
            }
        }
}
