package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.math.abs

/** OSM has limited precision of 7 decimals */
fun LatLon.equalsInOsm(other: LatLon) =
    !latitude.isDifferent(other.latitude, 1e-7)
    && !longitude.isDifferent(other.longitude, 1e-7)

private fun Double.isDifferent(other: Double, delta: Double) = abs(this - other) >= delta

// the resulting precision is about ~1 meter (see #1089):
// earth circumference / 360° / 10^5 => 40075017m / 360 / 100000 = 1.11m
fun LatLon.truncateTo5Decimals() = LatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())
