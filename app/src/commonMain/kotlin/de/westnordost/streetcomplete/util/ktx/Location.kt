package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.units.International

fun LocationMeasurement.toLocation(): Location =
    Location(
        position = position.toLatLon(),
        accuracyMeters = horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
        measuredAt = measuredAt,
    )

fun org.maplibre.spatialk.geojson.Position.toLatLon(): LatLon =
    LatLon(latitude, longitude)

fun LatLon.toPosition(): org.maplibre.spatialk.geojson.Position =
    org.maplibre.spatialk.geojson.Position(longitude = longitude, latitude = latitude)
