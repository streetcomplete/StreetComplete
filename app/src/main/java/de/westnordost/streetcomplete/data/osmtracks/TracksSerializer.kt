package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.ApplicationConstants
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

class TracksSerializer {
    fun serialize(trackpoints: List<Trackpoint>): String {
        return XML.encodeToString(trackpoints.toGpx())
    }
}

private fun List<Trackpoint>.toGpx() = Gpx(
    version = 1.0f,
    creator = ApplicationConstants.USER_AGENT,
    tracks = listOf(GpsTrack(listOf(GpsTrackSegment(map { it.toGpsTrackPoint() }))))
)

private fun Trackpoint.toGpsTrackPoint() = GpsTrackPoint(
    lat = position.latitude,
    lon = position.longitude,
    time = Instant.fromEpochMilliseconds(time),
    ele = elevation,
    hdop = accuracy
)

@Serializable
@XmlSerialName("gpx", namespace = "http://www.topografix.com/GPX/1/0")
private data class Gpx(
    val version: Float,
    val creator: String,
    @XmlSerialName("trk") val tracks: List<GpsTrack>,
)

@Serializable
private data class GpsTrack(
    @XmlSerialName("trkseg") val segments: List<GpsTrackSegment>
)

@Serializable
private data class GpsTrackSegment(
    @XmlSerialName("trkpt") val points: List<GpsTrackPoint>
)

@Serializable
private data class GpsTrackPoint(
    val lat: Double,
    val lon: Double,
    @XmlElement val time: Instant? = null,
    @XmlElement val ele: Float? = null,
    @XmlElement val hdop: Float? = null,
)
