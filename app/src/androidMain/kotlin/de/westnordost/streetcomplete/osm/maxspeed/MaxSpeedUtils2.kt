package de.westnordost.streetcomplete.osm.maxspeed

import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit

private val zoneRegex = Regex("([A-Z-]+):(?:zone:?)?([0-9]+)")

fun guessMaxspeedInKmh(tags: Map<String, String>, countryInfos: CountryInfos? = null): Float? {
    for (key in MAX_SPEED_TYPE_KEYS) {
        val value = tags[key] ?: continue
        when {
            value.endsWith("living_street")  -> return 10f
            value.endsWith("urban")          -> return 50f
            value.endsWith("nsl_restricted") -> return 50f
            value.endsWith("nsl_single")     -> return 60f
            value.endsWith("rural")          -> return 70f
            value.endsWith("nsl_dual")       -> return 70f
            value.endsWith("trunk")          -> return 100f
            value.endsWith("motorway")       -> return 120f
            value == "walk"                  -> return 5f
        }

        val matchResult = zoneRegex.matchEntire(value)
        if (matchResult != null) {
            val zoneSpeed = matchResult.groupValues[2].toFloatOrNull()
            val countryCode = matchResult.groupValues[1]
            val isMilesPerHour = countryInfos?.get(listOf(countryCode))?.speedUnits?.first()?.let {
                it == SpeedMeasurementUnit.MILES_PER_HOUR
            }
            if (zoneSpeed != null) {
                return if (isMilesPerHour == true) zoneSpeed * 1.609344f else zoneSpeed
            }
        }
    }
    return null
}
