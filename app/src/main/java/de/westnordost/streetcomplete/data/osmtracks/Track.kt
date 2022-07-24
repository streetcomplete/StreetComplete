package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class Trackpoint(
    val position: LatLon,
    /** timestamp in milliseconds */
    val time: Long,
    /** in meters */
    val horizontalDilutionOfPrecision: Float,
    /** in meters above sea level */
    val elevation: Float,
)
