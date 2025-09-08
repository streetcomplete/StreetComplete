package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.time.Duration.Companion.nanoseconds

fun android.location.Location.toLocation(): Location =
    Location(
        position = LatLon(latitude, longitude),
        accuracy = accuracy,
        elapsedDuration = elapsedRealtimeNanos.nanoseconds,
    )

fun android.location.Location.toLatLon(): LatLon =
    LatLon(latitude, longitude)
