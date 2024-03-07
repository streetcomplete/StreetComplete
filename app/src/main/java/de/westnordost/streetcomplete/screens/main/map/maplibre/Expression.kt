package de.westnordost.streetcomplete.screens.main.map.maplibre

import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*

/* expression for line width dependent on zoom (line width in property in meters) */
fun changeDistanceWithZoom(propertyName: String): Expression =
    interpolate(exponential(2), zoom(),
        stop(10, division(get(propertyName), literal(128))),
        stop(24, product(get(propertyName), literal(128)))
    )

fun isArea(): Expression =
    any(eq(geometryType(), "Polygon"), eq(geometryType(), "MultiPolygon"))

fun isLine(): Expression =
    any(eq(geometryType(), "LineString"), eq(geometryType(), "MultiLineString"))

fun isPoint(): Expression =
    any(eq(geometryType(), "Point"), eq(geometryType(), "MultiPoint"))
