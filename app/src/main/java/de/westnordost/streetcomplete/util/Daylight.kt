package de.westnordost.streetcomplete.util

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.time.LocalTime
import java.util.*

fun isDaylight(daylightTimes: DaylightTimes): Boolean {
    val (sunrise, sunset) = daylightTimes
    val now = LocalTime.now()

    // if sunset is after midnight
    return if (sunset < sunrise) {
        now > sunrise || now < sunset
    } else {
        now > sunrise && now < sunset
    }
}

data class DaylightTimes(val sunrise: LocalTime, val sunset: LocalTime)

fun getDaylightTimes(pos: LatLon): DaylightTimes {
    val timezone = TimeZone.getDefault().id
    val location = Location(pos.latitude, pos.longitude)
    val calculator = SolarEventCalculator(location, timezone)
    val today = Calendar.getInstance()

    return DaylightTimes(
        LocalTime.parse(calculator.computeSunriseTime(Zenith.CIVIL, today)),
        LocalTime.parse(calculator.computeSunsetTime(Zenith.CIVIL, today))
    )
}
