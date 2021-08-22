package de.westnordost.streetcomplete.util

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.time.LocalTime
import java.util.*

/** Returns whether it's currently light out. It will use the location of the node and check the
 *  civil sunrise/sunset time.  */
fun isDay(pos: LatLon): Boolean {
    val location = Location(pos.latitude, pos.longitude)
    val calculator = SolarEventCalculator(location, TimeZone.getDefault().id)
    val today = Calendar.getInstance()

    val sunrise = LocalTime.parse(calculator.computeSunriseTime(Zenith.CIVIL, today))
    val sunset = LocalTime.parse(calculator.computeSunsetTime(Zenith.CIVIL, today))
    val now = LocalTime.now()

    // if sunset is after midnight
    return if (sunset < sunrise) {
        now > sunrise || now < sunset
    } else {
        now > sunrise && now < sunset
    }
}
