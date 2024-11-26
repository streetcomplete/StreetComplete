package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.ktx.format

fun parseGeoUri(uri: String): GeoLocation? {
    if (!uri.startsWith("geo:")) return null

    val geoUriRegex = Regex("(-?[0-9]*\\.?[0-9]+),(-?[0-9]*\\.?[0-9]+).*?(?:\\?z=([0-9]*\\.?[0-9]+))?")
    val match = geoUriRegex.matchEntire(uri.substringAfter("geo:")) ?: return null

    val latitude = match.groupValues[1].toDoubleOrNull() ?: return null
    if (latitude < -90 || latitude > +90) return null
    val longitude = match.groupValues[2].toDoubleOrNull() ?: return null
    if (longitude < -180 || longitude > +180) return null

    // zoom is optional. If it is invalid, we treat it the same as if it is not there
    val zoom = match.groupValues[3].toDoubleOrNull()

    return GeoLocation(latitude, longitude, zoom)
}

fun buildGeoUri(latitude: Double, longitude: Double, zoom: Double? = null): String {
    val z = if (zoom != null) "?z=$zoom" else ""
    val lat = latitude.format(5)
    val lon = longitude.format(5)
    return "geo:$lat,$lon$z"
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double?
)
