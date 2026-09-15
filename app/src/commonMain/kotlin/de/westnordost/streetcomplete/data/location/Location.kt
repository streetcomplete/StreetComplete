package de.westnordost.streetcomplete.data.location

import androidx.compose.runtime.Immutable
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.time.Duration

@Immutable
data class Location(
    val position: LatLon,
    val accuracy: Float,
    val elapsedDuration: Duration,
)
