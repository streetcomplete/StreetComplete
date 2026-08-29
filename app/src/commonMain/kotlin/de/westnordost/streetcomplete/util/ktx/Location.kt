package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.units.International

fun org.maplibre.compose.location.Location.toLocation(): Location =
    Location(
        position = position.value.toLatLon(),
        accuracy = position.accuracy?.toFloat(International.Meters) ?: 0f,
        elapsedDuration = timestamp.elapsedNow(),
    )

fun org.maplibre.spatialk.geojson.Position.toLatLon(): LatLon =
    LatLon(latitude, longitude)
