package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.MultiLineString
import org.maplibre.spatialk.geojson.Position

fun LatLon.toPosition(): Position =
    Position(longitude = longitude, latitude = latitude)

fun List<LatLon>.toLineGeometry(): LineString? =
    if (size < 2) null else LineString(map { it.toPosition() })

fun List<List<LatLon>>.toMultiLineGeometry(): MultiLineString =
    MultiLineString(mapNotNull { line -> line.toLineGeometry()?.coordinates })
