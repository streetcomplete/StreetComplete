package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.core.impl.newReader
import nl.adaptivity.xmlutil.xmlStreaming

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
fun parseGpxFile(inputStream: InputStream): Flow<TrackSegment> =
    xmlStreaming.newReader(inputStream, "UTF-8").parseGpxFile()

private fun XmlReader.parseGpxFile(): Flow<TrackSegment> {
    return flow {
        var depth = 1

        while (depth != 0) {
            when (this@parseGpxFile.next()) {
                END_ELEMENT -> {
                    depth--
                }

                START_ELEMENT -> when (localName) {
                    SEGMENT -> {
                        // segment is closed while parsing, thus depth remains the same
                        emit(
                            parseSegment(this@parseGpxFile)
                        )
                    }

                    else -> {
                        depth++
                    }
                }

                END_DOCUMENT -> {
                    break
                }

                else -> {}
            }
        }
    }
}

private fun parseSegment(xmlReader: XmlReader): TrackSegment = flow {
    var depth = 1
    while (depth != 0) {
        when (xmlReader.next()) {
            END_ELEMENT -> {
                if (xmlReader.localName == SEGMENT) {
                    return@flow
                }

                depth--
            }

            START_ELEMENT -> {
                if (xmlReader.localName == TRACK_POINT) {
                    emit(
                        LatLon(
                            // TODO [sgr]: correct null check
                            xmlReader.getAttributeValue(null, "lat")!!.toDouble(),
                            xmlReader.getAttributeValue(null, "lon")!!.toDouble()
                        )
                    )
                }
                depth++
            }

            END_DOCUMENT -> {
                break
            }

            else -> {}

        }
    }
}
