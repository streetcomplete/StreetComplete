package de.westnordost.streetcomplete.screens.main.map2

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.Feature
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.any
import org.maplibre.compose.expressions.dsl.coalesce
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.contains
import org.maplibre.compose.expressions.dsl.convertToBoolean
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.div
import org.maplibre.compose.expressions.dsl.dp
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.exponential
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.neq
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.times
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.GeometryType
import org.maplibre.compose.expressions.value.NumberValue
import org.maplibre.compose.expressions.value.StringValue
import kotlin.math.PI
import kotlin.math.cos

fun fadeInAtZoom(start: Float, range: Float = 1f, endOpacity: Float = 1f) =
    byZoom(start to 0f, start+range to endOpacity)

fun fadeOutAtZoom(start: Float, range: Float = 1f, startOpacity: Float = 1f) =
    byZoom(start to startOpacity, start+range to 0f)

@JvmName("byZoomFloat")
fun byZoom(vararg stops: Pair<Number, Float>) =
    interpolate(exponential(2f), zoom(), *stops.map { it.first to const(it.second) }.toTypedArray())

@JvmName("byZoomDp")
fun byZoom(vararg stops: Pair<Number, Dp>) =
    interpolate(exponential(2f), zoom(), *stops.map { it.first to const(it.second) }.toTypedArray())

@JvmName("byZoomTextUnit")
fun byZoom(vararg stops: Pair<Number, TextUnit>) =
    interpolate(exponential(2f), zoom(), *stops.map { it.first to const(it.second) }.toTypedArray())

/** Returns whether this feature has the given [key]-[value] pair */
fun Feature.has(key: String, value: String) =
    get(key).convertToString() eq const(value)

/** Returns whether this feature has the given [key]-[value] pair */
fun Feature.has(key: String, value: Int) =
    get(key).convertToNumber() eq const(value)

/** Returns whether this feature has the given [key]-[value] pair */
fun Feature.has(key: String, value: Boolean) =
    get(key).convertToBoolean() eq const(value)

/** Returns whether this feature has a [key]-value pair of which the value is in of the given
 * [values] */
fun Feature.hasAny(key: String, values: List<String>) =
    const(values).contains(get(key))

fun Feature.isPoint() =
    geometryType() eq const(GeometryType.Point)

fun Feature.isLines() =
    any(
        geometryType() eq const(GeometryType.LineString),
        geometryType() eq const(GeometryType.MultiLineString)
    )

fun Feature.isArea() =
    any(
        geometryType() eq const(GeometryType.Polygon),
        geometryType() eq const(GeometryType.MultiPolygon)
    )

/** Get an expression that resolves to the localized name.
 *  If the localized name in the user's [language] is the same as the primary name, then only this
 *  name is displayed. Otherwise, the primary name is displayed, then the localized name below */
fun Feature.localizedName(
    languages: List<String>,
    nameKey: String,
    localizedNameKey: (String) -> String,
    extraNameKeys: List<String>
): Expression<StringValue> {
    val localizedNameKeys = languages.map(localizedNameKey) + extraNameKeys
    val getLocalizedName = coalesce(*localizedNameKeys.map { get(it) }.toTypedArray())
    val getName = get(nameKey).cast<StringValue>()
    return switch(
        // localized name set and different as main name -> show both
        condition(
            test = all(getLocalizedName.convertToBoolean(), getName neq getLocalizedName.cast()),
            output = getName + const("\n") + getLocalizedName.cast()
        ),
        // otherwise just show the name
        fallback = getName
    )
}

fun inMeters(
    width: Expression<NumberValue<Number>>,
    latitude: Double = 30.0
): Expression<NumberValue<Dp>> {
    // the more north you go, the smaller of an area each mercator tile actually covers
    // the additional factor of 1.20 comes from a simple measuring test with a ruler on a
    // smartphone screen done at approx. latitude = 0 and latitude = 70, i.e. without it, lines are
    // drawn at both latitudes approximately 20% too large ¯\_(ツ)_/¯
    val sizeFactor = (cos(PI * latitude / 180) * 1.2).toFloat()
    return interpolate(
        exponential(2f), zoom(),
        8 to width / const(256) / const(sizeFactor),
        24 to width * const(256) / const(sizeFactor)
    ).dp
}

fun inMeters(
    width: Float,
    latitude: Double = 30.0
): Expression<NumberValue<Dp>> {
    val sizeFactor = (cos(PI * latitude / 180) * 1.2).toFloat()
    return interpolate(
        exponential(2f), zoom(),
        8 to const(width) / const(256) / const(sizeFactor),
        24 to const(width) * const(256) / const(sizeFactor)
    ).dp
}
