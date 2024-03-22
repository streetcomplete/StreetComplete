package de.westnordost.streetcomplete.screens.main.map.maplibre

import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.*

/* expression for line width dependent on zoom (line width in property in meters) */
fun inMeters(expression: Expression): Expression =
    interpolate(exponential(2), zoom(),
        stop(7.5, division(expression, literal(512))),
        stop(24.5, product(expression, literal(512)))
    )

fun inMeters(width: Float): Expression =
    interpolate(exponential(2), zoom(),
        stop(7.5, width / 512),
        stop(24.5, width * 512)
    )

fun isArea(): Expression =
    any(eq(geometryType(), "Polygon"), eq(geometryType(), "MultiPolygon"))

fun isLine(): Expression =
    any(eq(geometryType(), "LineString"), eq(geometryType(), "MultiLineString"))

fun isPoint(): Expression =
    any(eq(geometryType(), "Point"), eq(geometryType(), "MultiPoint"))
