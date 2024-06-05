package de.westnordost.streetcomplete.data.osmtracks

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("gpx", namespace = "http://www.topografix.com/GPX/1/0")
data class Gpx(
    val version: Float,
    val creator: String,
    val tracks: List<GpsTrack>,
)

@Serializable
@SerialName("trk")
data class GpsTrack(val segments: List<GpsTrackSegment>)

@Serializable
@SerialName("trkseg")
data class GpsTrackSegment(val points: List<GpsTrackPoint>)

@Serializable
@SerialName("trkpt")
data class GpsTrackPoint(
    val lat: Double,
    val lon: Double,
    @XmlElement
    val time: Instant? = null,
    @XmlElement
    val ele: Float? = null,
    @XmlElement
    val hdop: Float? = null,
)
