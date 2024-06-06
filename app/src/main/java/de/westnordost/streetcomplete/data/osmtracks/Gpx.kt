package de.westnordost.streetcomplete.data.osmtracks

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("gpx", namespace = "http://www.topografix.com/GPX/1/0")
data class Gpx(
    val version: Float,
    val creator: String,
    @XmlSerialName("trk") val tracks: List<GpsTrack>,
)

@Serializable
data class GpsTrack(
    @XmlSerialName("trkseg") val segments: List<GpsTrackSegment>
)

@Serializable
data class GpsTrackSegment(
    @XmlSerialName("trkpt") val points: List<GpsTrackPoint>
)

@Serializable
data class GpsTrackPoint(
    val lat: Double,
    val lon: Double,
    @XmlElement val time: Instant? = null,
    @XmlElement val ele: Float? = null,
    @XmlElement val hdop: Float? = null,
)
