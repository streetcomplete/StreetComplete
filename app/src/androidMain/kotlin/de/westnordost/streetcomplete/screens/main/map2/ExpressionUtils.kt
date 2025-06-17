package de.westnordost.streetcomplete.screens.main.map2

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import dev.sargunv.maplibrecompose.expressions.ast.Expression
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.all
import dev.sargunv.maplibrecompose.expressions.dsl.asBoolean
import dev.sargunv.maplibrecompose.expressions.dsl.asNumber
import dev.sargunv.maplibrecompose.expressions.dsl.asString
import dev.sargunv.maplibrecompose.expressions.dsl.coalesce
import dev.sargunv.maplibrecompose.expressions.dsl.condition
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.contains
import dev.sargunv.maplibrecompose.expressions.dsl.convertToBoolean
import dev.sargunv.maplibrecompose.expressions.dsl.eq
import dev.sargunv.maplibrecompose.expressions.dsl.exponential
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.interpolate
import dev.sargunv.maplibrecompose.expressions.dsl.neq
import dev.sargunv.maplibrecompose.expressions.dsl.plus
import dev.sargunv.maplibrecompose.expressions.dsl.switch
import dev.sargunv.maplibrecompose.expressions.dsl.zoom
import dev.sargunv.maplibrecompose.expressions.value.GeometryType
import dev.sargunv.maplibrecompose.expressions.value.StringValue

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
    get(key).asString() eq const(value)

/** Returns whether this feature has the given [key]-[value] pair */
fun Feature.has(key: String, value: Int) =
    get(key).asNumber() eq const(value)

/** Returns whether this feature has the given [key]-[value] pair */
fun Feature.has(key: String, value: Boolean) =
    get(key).asBoolean() eq const(value)

/** Returns whether this feature has a [key]-value pair of which the value is in of the given
 * [values] */
fun Feature.hasAny(key: String, values: List<String>) =
    const(values).contains(get(key))

fun Feature.isPoint() =
    feature.type() eq const(GeometryType.Point)

fun Feature.isLines() =
    const(listOf(const(GeometryType.LineString), const(GeometryType.MultiLineString)))
        .contains(feature.type())

fun Feature.isArea() =
    const(listOf(const(GeometryType.Polygon), const(GeometryType.MultiPolygon)))
        .contains(feature.type())

/** Get an expression that resolves to the localized name.
 *  If the localized name in the user's [language] is the same as the primary name, then only this
 *  name is displayed. Otherwise, the primary name is displayed, then the localized name below */
fun Feature.localizedName(
    languages: List<String>,
    nameKey: String,
    localizedNameKey: (String) -> String,
    extraLocalizedNameKeys: List<String>
): Expression<StringValue> {
    val localizedNameKeys = languages.map(localizedNameKey) + extraLocalizedNameKeys
    val getLocalizedName = coalesce(
        *localizedNameKeys.map { feature.get(it).asString() }.toTypedArray()
    )
    val getName = feature.get(nameKey).asString()
    return switch(
        // localized name set and different as main name -> show both
        condition(
            test = all(getLocalizedName.convertToBoolean(), getName neq getLocalizedName),
            output = getName + const("\n") + getLocalizedName
        ),
        // otherwise just show the name
        fallback = getName
    )
}
