package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.compose.location.LocationEvent
import org.maplibre.spatialk.units.International
import kotlin.time.TimeSource

private val locationTimeMark = TimeSource.Monotonic.markNow()

fun LocationEvent.Update.toLocation(): Location =
    Location(
        position = measurement.position.toLatLon(),
        accuracy = measurement.horizontalAccuracy?.toFloat(International.Meters) ?: 0f,
        elapsedDuration = locationTimeMark.elapsedNow() - measurementMark.elapsedNow(),
    )

fun org.maplibre.spatialk.geojson.Position.toLatLon(): LatLon =
    LatLon(latitude, longitude)
