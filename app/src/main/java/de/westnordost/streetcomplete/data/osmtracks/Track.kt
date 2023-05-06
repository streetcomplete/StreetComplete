package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Trackpoint(
    val position: LatLon,
    /** timestamp in milliseconds */
    val time: Long,
    /** in meters */
    @JsonNames("horizontalDilutionOfPrecision")
    val accuracy: Float,
    /** in meters above sea level */
    val elevation: Float,
)

// the JsonNames is only for backward compatibility before horizontalDilutionOfPrecision was changed to accuracy
