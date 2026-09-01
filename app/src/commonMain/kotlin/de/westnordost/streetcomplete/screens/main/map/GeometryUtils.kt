package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.maplibre.spatialk.geojson.Position

fun LatLon.toPosition(): Position =
    Position(longitude = longitude, latitude = latitude)
