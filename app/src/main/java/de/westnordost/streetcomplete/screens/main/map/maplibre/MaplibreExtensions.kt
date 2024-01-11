package de.westnordost.streetcomplete.screens.main.map.maplibre

import com.mapbox.geojson.Point
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

fun pointFromGeometry(geometry: ElementGeometry): Point {
    return Point.fromLngLat(geometry.center.longitude, geometry.center.latitude)
}
