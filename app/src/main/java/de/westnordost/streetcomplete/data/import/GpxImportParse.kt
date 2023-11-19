package de.westnordost.streetcomplete.data.import

import android.util.Xml
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private const val TRACK_POINT = "trkpt"
private const val TRACK = "trk"

/**
 * Parses the track points of the first track from a GPX file.
 *
 * As the standard doesn't require multiple tracks to be connected, and this lightweight parser
 * function is intended to provide a single consecutive track, further tracks are ignored.
 * Multiple track segments within the first track are parsed however, assuming any gaps are
 * negligible and can be interpolated meaningfully during subsequent processing.
 *
 * @param inputStream valid XML according to http://www.topografix.com/GPX/1/1 schema
 */
fun parseSingleGpxTrack(inputStream: InputStream): List<LatLon> {
    // TODO sgr: if this should return a sequence, would need to pass e.g. a Sequence<String> from
    // File.useLines to make sure the file is correctly closed; inputStream.use closes it
    // immediately upon return, leading to an exception when requesting the next element of the sequence
    inputStream.use {
        val xpp = Xml.newPullParser()
        xpp.setInput(inputStream, "UTF-8")
        try {
            return parseSingleGpxTrack(xpp).toList()
        } catch (e: Exception) {
            throw e;
        }
    }
}

/**
 * @param parser a pull parser with input already set, ready to call nextTag
 */
@Throws(XmlPullParserException::class, IOException::class)
fun parseSingleGpxTrack(parser: XmlPullParser): Sequence<LatLon> = sequence {
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)

    parser.nextTag()
    parser.require(XmlPullParser.START_TAG, "http://www.topografix.com/GPX/1/1", "gpx")

    var depth = 1
    while (depth != 0) {
        when (parser.next()) {
            XmlPullParser.END_TAG -> {
                if (parser.name == TRACK) {
                    return@sequence
                }
                depth--
            }

            XmlPullParser.START_TAG -> {
                if (parser.name == TRACK_POINT) {
                    yield(
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
