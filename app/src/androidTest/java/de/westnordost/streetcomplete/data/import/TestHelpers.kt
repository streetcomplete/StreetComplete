package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList

data class TrackPoint(
    val lat: String,
    val lon: String,
)

internal fun LatLon.toTrackPoint(): TrackPoint {
    return TrackPoint(this.latitude.toString(), this.longitude.toString())
}

internal fun TrackPoint.toLatLon(): LatLon {
    return LatLon(this.lat.toDouble(), this.lon.toDouble())
}

// explicitly not using LatLon to allow testing wrong and special string formatting
internal fun minimalGpxBuilder(trackPoints: List<TrackPoint>): String = buildString {
    append("<gpx version='1.1' xmlns='http://www.topografix.com/GPX/1/1'>")
    append("<trk>")
    append("<trkseg>")
    trackPoints.forEach {
        append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
    }
    append("</trkseg>")
    append("</trk>")
    append("</gpx>")
}

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun parseGpx(input: String): List<LatLon> {
    // make sure flows are exhausted in correct order
    return parseGpxFile(input.byteInputStream()).flatMapConcat { it }.toList()
}

