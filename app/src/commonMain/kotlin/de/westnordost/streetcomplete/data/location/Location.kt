package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.time.Duration

data class Location(
    val position: LatLon,
    val accuracy: Float,
    val elapsedDuration: Duration,
)
