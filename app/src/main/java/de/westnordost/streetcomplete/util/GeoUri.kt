package de.westnordost.streetcomplete.util

import android.net.Uri
import androidx.core.net.toUri
import de.westnordost.streetcomplete.util.ktx.format

fun parseGeoUri(uri: Uri): GeoLocation? {
    if (uri.scheme != "geo") return null

    val geoUriRegex = Regex("(-?[0-9]*\\.?[0-9]+),(-?[0-9]*\\.?[0-9]+).*?(?:\\?z=([0-9]*\\.?[0-9]+))?")
    val match = geoUriRegex.matchEntire(uri.schemeSpecificPart) ?: return null

    val latitude = match.groupValues[1].toDoubleOrNull() ?: return null
    if (latitude < -90 || latitude > +90) return null
    val longitude = match.groupValues[2].toDoubleOrNull() ?: return null
    if (longitude < -180 || longitude > +180) return null

    // zoom is optional. If it is invalid, we treat it the same as if it is not there
    val zoom = match.groupValues[3].toDoubleOrNull()

    return GeoLocation(latitude, longitude, zoom)
}

fun buildGeoUri(latitude: Double, longitude: Double, zoom: Double? = null): Uri {
    val z = if (zoom != null) "?z=$zoom" else ""
    val lat = latitude.format(5)
    val lon = longitude.format(5)
    val geoUri = "geo:$lat,$lon$z"
    return geoUri.toUri()
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double?
)
