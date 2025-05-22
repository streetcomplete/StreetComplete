package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit

val MAXSPEED_TYPE_KEYS = setOf(
    "source:maxspeed",
    "zone:maxspeed",
    "maxspeed:type",
    "zone:traffic"
)

private val anyMaxSpeedTagKey = "~${(MAXSPEED_TYPE_KEYS + "maxspeed").joinToString("|")}"

val isImplicitMaxSpeed = "(!maxspeed or $anyMaxSpeedTagKey ~ \"implicit|([A-Z-]+:.*)\")"

val isInSlowZone = "$anyMaxSpeedTagKey ~ \"[A-Z-]+:(zone:?)?([1-9]|[1-2][0-9]|30)\""

val isImplicitMaxSpeedButNotSlowZone = "$isImplicitMaxSpeed and !($isInSlowZone)"

/** Functions to get speed in km/h from tags */

fun getMaxspeedInKmh(tags: Map<String, String>): Float? {
    val speed = tags["maxspeed"] ?: return null
    return if (speed.endsWith(" mph")) {
        val mphSpeed = speed.substring(0, speed.length - 4).toFloatOrNull()
        if (mphSpeed != null) mphSpeed * 1.609344f else null
    } else {
        speed.toFloatOrNull()
    }
}

private val zoneRegex = Regex("([A-Z-]+):(?:zone:?)?([0-9]+)")

fun guessMaxspeedInKmh(tags: Map<String, String>, countryInfos: CountryInfos? = null): Float? {
    for (key in (MAXSPEED_TYPE_KEYS + "maxspeed")) {
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
