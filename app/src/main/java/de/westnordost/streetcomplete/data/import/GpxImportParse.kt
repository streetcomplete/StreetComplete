package de.westnordost.streetcomplete.data.import

import android.util.Xml
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private const val TRACK_POINT = "trkpt"
private const val SEGMENT = "trkseg"

typealias TrackSegment = Flow<LatLon>

/**
 * Parses all track points from a GPX file.
 *
 * Yields consecutive segments. Track points within a TrackSegment can be interpolated meaningfully
 * during subsequent processing, while track points from different TrackSegments cannot be assumed
 * to be connected.
 *
 * @param inputStream valid XML according to http://www.topografix.com/GPX/1/1 schema
 * Note: the caller is responsible to close the inputStream as appropriate;
 * calls to the resulting sequence after closing the inputStream may fail.
 * @return a sequence of TrackSegments, i.e. sequences of track points logically connected.
 *
 * Note: The nested sequences returned work on a single input stream, thus make sure to exhaust
 * any TrackSegment first before proceeding to the next one. A TrackSegment will not yield any
 * more track points once you proceed to the next TrackSegment in the outer sequence, thus e.g.
 * result.toList().first().toList() will not yield any element, while
 * result.flatMap { it.toList() }.toList() will.
 */
@Throws(XmlPullParserException::class, IOException::class)
fun parseGpxFile(inputStream: InputStream): Flow<TrackSegment> {
    val parser = Xml.newPullParser()
    parser.setInput(inputStream, "UTF-8")

    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)

    parser.nextTag()
    parser.require(XmlPullParser.START_TAG, "http://www.topografix.com/GPX/1/1", "gpx")

    return flow {
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> {
                    depth--
                }

                XmlPullParser.START_TAG -> {
                    if (parser.name == SEGMENT) {
                        // segment is closed while parsing, thus depth remains the same
                        emit(
                            parseSegment(parser)
                        )
                    } else {
                        depth++
                    }
                }

                XmlPullParser.END_DOCUMENT -> {
                    break
                }
            }
        }
    }
}

@Throws(XmlPullParserException::class, IOException::class)
private fun parseSegment(parser: XmlPullParser): TrackSegment = flow {
    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlPullParser.END_TAG -> {
                if (parser.name == SEGMENT) {
                    return@flow
                }
                depth--
            }

            XmlPullParser.START_TAG -> {
                if (parser.name == TRACK_POINT) {
                    emit(
                        LatLon(
                            parser.getAttributeValue(null, "lat").toDouble(),
                            parser.getAttributeValue(null, "lon").toDouble()
                        )
                    )
                }
                depth++
            }

            XmlPullParser.END_DOCUMENT -> {
                break
            }
        }
    }
}
