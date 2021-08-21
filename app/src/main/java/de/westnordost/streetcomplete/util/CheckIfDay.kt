package de.westnordost.streetcomplete.util

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

fun localDateToCalendar(localDate: LocalDate): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(localDate.year, localDate.month.value, localDate.dayOfMonth)
    return calendar
}

fun isDay(pos: LatLon): Boolean {
    /* This functions job is to check if it's currently light out.
    It will use the location of the node and check the civil sunrise/sunset time.

    Sometimes sunset is after midnight. This would actually cause sunset to be before sunrise (as it's checking 00:00 to 23:59, it'll catch the day before!), so to gnt around this, we check the next day if that's the case.
     */

    val timezone = TimeZone.getDefault().id
    val location = Location(pos.latitude, pos.longitude)
    val calculator = SolarEventCalculator(location, timezone)
    val now = ZonedDateTime.now(ZoneId.of(timezone))
    val today = now.toLocalDate()

    val sunrise = ZonedDateTime.of(today, LocalTime.parse(calculator.computeSunriseTime(Zenith.CIVIL, localDateToCalendar(today))), ZoneId.of(timezone))
    val sunset = ZonedDateTime.of(today, LocalTime.parse(calculator.computeSunsetTime(Zenith.CIVIL, localDateToCalendar(today))), ZoneId.of(timezone))
    return if (sunset < sunrise) {

        val sunset = ZonedDateTime.of(today.plusDays(1), LocalTime.parse(calculator.computeSunsetTime(Zenith.CIVIL, localDateToCalendar(today.plusDays(1)))), ZoneId.of(timezone))
        now in sunrise..sunset
    } else {
        now in sunrise..sunset
    }
}
