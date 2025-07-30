package de.westnordost.streetcomplete.screens.main.map.maplibre

import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.*
import kotlin.math.PI

fun inMeters(expression: Expression, latitude: Double = 30.0): Expression {
    // the more north you go, the smaller of an area each mercator tile actually covers
    // the additional factor of 1.20 comes from a simple measuring test with a ruler on a
    // smartphone screen done at approx. latitude = 0 and latitude = 70, i.e. without it, lines are
    // drawn at both latitudes approximately 20% too large
    val sizeFactor = kotlin.math.cos(PI * latitude / 180) * 1.2
    return interpolate(
        exponential(2), zoom(),
        stop(8, division(division(expression, literal(256)), literal(sizeFactor))),
        stop(24, division(product(expression, literal(256)), literal(sizeFactor)))
    )
}

fun inMeters(width: Float, latitude: Double = 30.0): Expression {
    val sizeFactor = kotlin.math.cos(PI * latitude / 180) * 1.2
    return interpolate(
        exponential(2), zoom(),
        stop(8, width / 256 / sizeFactor),
        stop(24, width * 256 / sizeFactor)
    )
}

fun isArea(): Expression =
    any(eq(geometryType(), "Polygon"), eq(geometryType(), "MultiPolygon"))

fun isLine(): Expression =
    any(eq(geometryType(), "LineString"), eq(geometryType(), "MultiLineString"))

fun isPoint(): Expression =
    any(eq(geometryType(), "Point"), eq(geometryType(), "MultiPoint"))
