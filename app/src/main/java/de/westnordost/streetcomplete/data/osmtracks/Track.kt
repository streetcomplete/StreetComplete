package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: Long,
    val userName: String,
)

@Serializable
data class Trackpoint(
    val position: LatLon,
    val time: Long,
    val horizontalDilutionOfPrecision: Float,
    val elevation: Float,
)
