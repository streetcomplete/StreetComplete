package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.units.International

fun org.maplibre.compose.location.LocationMeasurement.toLocation(): Location =
    Location(
        position = position.toLatLon(),
        accuracy = horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
        measuredAt = measuredAt,
    )

fun org.maplibre.spatialk.geojson.Position.toLatLon(): LatLon =
    LatLon(latitude, longitude)
