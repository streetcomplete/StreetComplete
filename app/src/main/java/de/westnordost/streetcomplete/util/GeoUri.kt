package de.westnordost.streetcomplete.util

import android.net.Uri
import java.util.*

fun parseGeoUri(uri: Uri): GeoLocation? {
    if (uri.scheme != "geo") return null

    val geoUriRegex = Regex("(-?[0-9]*\\.?[0-9]+),(-?[0-9]*\\.?[0-9]+).*?(?:\\?z=([0-9]*\\.?[0-9]+))?")
    val match = geoUriRegex.matchEntire(uri.schemeSpecificPart) ?: return null

    val latitude = match.groupValues[1].toDoubleOrNull() ?: return null
    if (latitude < -90 || latitude > +90) return null
    val longitude = match.groupValues[2].toDoubleOrNull() ?: return null
    if (longitude < -180 && longitude > +180) return null

    // zoom is optional. If it is invalid, we treat it the same as if it is not there
    val zoom = match.groupValues[3].toFloatOrNull()

    return GeoLocation(latitude, longitude, zoom)
}

fun buildGeoUri(latitude: Double, longitude: Double, zoom: Float? = null): Uri {
    val zoomStr = if (zoom != null) "?z=$zoom" else ""
    val geoUri = Formatter(Locale.US).format("geo:%.5f,%.5f%s", latitude, longitude, zoomStr).toString()
    return Uri.parse(geoUri)
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float?
)
