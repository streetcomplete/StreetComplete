package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.ktx.attribute
import de.westnordost.streetcomplete.util.ktx.endTag
import de.westnordost.streetcomplete.util.ktx.startTag
import kotlinx.datetime.Instant
import nl.adaptivity.xmlutil.XmlWriter
import nl.adaptivity.xmlutil.newWriter
import nl.adaptivity.xmlutil.xmlStreaming

class TracksSerializer {
    fun serialize(trackpoints: List<Trackpoint>): String {
        val buffer = StringBuilder()
        xmlStreaming.newWriter(buffer).serializeToGpx(trackpoints)
        return buffer.toString()
    }
}

private fun XmlWriter.serializeToGpx(trackpoints: List<Trackpoint>) {
    startTag("gpx")
    attribute("xmlns", "http://www.topografix.com/GPX/1/0")
    attribute("version", "1.0")
    attribute("creator", ApplicationConstants.USER_AGENT)
    startTag("trk")
    startTag("trkseg")
    for (trackpoint in trackpoints) {
        startTag("trkpt")
        attribute("lat", trackpoint.position.latitude.toString())
        attribute("lon", trackpoint.position.longitude.toString())
        startTag("time")
        text(Instant.fromEpochMilliseconds(trackpoint.time).toString())
        endTag("time")
        startTag("ele")
        text(trackpoint.elevation.toString())
        endTag("ele")
        startTag("hdop")
        text(trackpoint.accuracy.toString())
        endTag("hdop")
        endTag("trkpt")
    }
    endTag("trkseg")
    endTag("trk")
    endTag("gpx")
}
