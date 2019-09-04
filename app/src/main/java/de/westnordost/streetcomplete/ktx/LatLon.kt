package de.westnordost.streetcomplete.ktx

import de.westnordost.osmapi.map.data.LatLon
import kotlin.math.abs

/** OSM has limited precision of 7 decimals */
fun LatLon.equalsInOsm(other: LatLon) =
    !latitude.isDifferent(other.latitude, 1e-7) &&
    !longitude.isDifferent(other.longitude, 1e-7)

private fun Double.isDifferent(other: Double, delta: Double) = abs(this - other) >= delta
