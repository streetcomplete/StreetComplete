package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import java.time.Month.*

fun isWinter(location: LatLon): Boolean {
    val now = systemTimeNow().toLocalDate()
    val winterSeason = if (location.latitude > 0)
        listOf(NOVEMBER, DECEMBER, JANUARY, FEBRUARY, MARCH, APRIL)
    else listOf(JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER)
    return now.month in winterSeason
}
