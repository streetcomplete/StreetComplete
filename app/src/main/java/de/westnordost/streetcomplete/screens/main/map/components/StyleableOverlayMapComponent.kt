package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import androidx.annotation.UiThread
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.overlays.Color.INVISIBLE
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.screens.main.map.maplibre.changeDistanceWithZoom
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isLine
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.toRGB

/** Takes care of displaying styled map data */
class StyleableOverlayMapComponent(private val context: Context, private val map: MapLibreMap) {

    private val overlaySource = GeoJsonSource(
        SOURCE,
        GeoJsonOptions().withMinZoom(16)
    )

    private val darkenedColors = HashMap<String, String>()

    private val sideLinesProperties = arrayOf(
        lineCap(Property.LINE_CAP_BUTT),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineColor(get("color")),
        lineOpacity(get("opacity")),
        lineOffset(changeDistanceWithZoom("offset")),
        lineWidth(changeDistanceWithZoom("width")),
    )

    private val sideLineFilters = arrayOf(
        isLine(),
        gte(zoom(), 16f),
        has("offset"),
    )

    val sideLayers: List<Layer> = listOf(
        LineLayer("overlay-lines-side", SOURCE)
            .withFilter(all(*sideLineFilters, not(has("bridge")), not(has("dashed"))))
            .withProperties(*sideLinesProperties),
        LineLayer("overlay-lines-dashed-side", SOURCE)
            .withFilter(all(*sideLineFilters, not(has("bridge")), has("dashed")))
            .withProperties(*sideLinesProperties, lineDasharray(arrayOf(1.5f, 1f))),
    )

    val sideLayersBridge: List<Layer> = listOf(
        LineLayer("overlay-lines-bridge-side", SOURCE)
            .withFilter(all(*sideLineFilters, has("bridge"), not(has("dashed"))))
            .withProperties(*sideLinesProperties),
        LineLayer("overlay-lines-dashed-bridge-side", SOURCE)
            .withFilter(all(*sideLineFilters, has("bridge"), has("dashed")))
            .withProperties(*sideLinesProperties, lineDasharray(arrayOf(1.5f, 1f))),
    )

    val layers: List<Layer> = listOf(
        LineLayer("overlay-lines-casing", SOURCE)
            .withFilter(all(
                isLine(),
                gte(zoom(), 16f),
                not(has("offset")),
                not(has("dashed"))
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(get("outline-color")),
                lineOpacity(get("opacity")),
                lineGapWidth(changeDistanceWithZoom("width")),
                lineWidth(changeDistanceWithZoom(1f)),
            ),
        FillLayer("overlay-fills", SOURCE)
            .withFilter(all(
                isArea(),
                gte(zoom(), 16f)
            ))
            .withProperties(
                fillColor(get("color")),
                fillOpacity(get("opacity")),
            ),
        LineLayer("overlay-lines", SOURCE)
            .withFilter(all(
                isLine(),
                gte(zoom(), 16f),
                not(has("offset")),
                not(has("dashed"))
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                lineWidth(changeDistanceWithZoom("width")),
            ),
        LineLayer("overlay-lines-dashed", SOURCE)
            .withFilter(all(
                isLine(),
                gte(zoom(), 16f),
                not(has("offset")),
                has("dashed")
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT), // because of dashed
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                lineWidth(changeDistanceWithZoom("width")),
                lineDasharray(arrayOf(1.5f, 1f)),
            ),
        LineLayer("overlay-fills-outline", SOURCE)
            .withFilter(all(
                isArea(),
                gte(zoom(), 16f)
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("outline-color")),
                lineOpacity(get("opacity")),
                lineWidth(changeDistanceWithZoom(1f)),
            ),
        FillExtrusionLayer("overlay-heights", SOURCE)
            .withFilter(all(
                isArea(),
                has("height"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                fillExtrusionColor(get("color")),
                fillExtrusionOpacity(get("opacity")),
                fillExtrusionHeight(get("height")),
            ),
        SymbolLayer("overlay-symbols", SOURCE)
            .withFilter(all(
                gte(zoom(), 16f),
                isPoint()
            ))
            .withProperties(
                iconImage(get("icon")),

                textField(get("label")),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textJustify(Property.TEXT_JUSTIFY_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textSize(16 * context.resources.configuration.fontScale),
                textHaloColor("white"),
                textHaloWidth(1.5f),
                iconColor("black"),
                iconHaloColor("white"),
                iconHaloWidth(1.5f),
                iconHaloBlur(2f),
                textOptional(true),
                iconAllowOverlap(step(zoom(), literal(false), stop(18, true))),
                textAllowOverlap(step(zoom(), literal(false), stop(20, true))),
            )
    )

    /** Shows/hides the map data */
    var isVisible: Boolean
        // add / remove source
        @UiThread get() = layers.first().visibility.value != Property.NONE
        @UiThread set(value) {
            if (isVisible == value) return
            if (value) {
                layers.forEach { it.setProperties(visibility(Property.VISIBLE)) }
            } else {
                layers.forEach { it.setProperties(visibility(Property.NONE)) }
            }
        }

    init {
        map.style?.addSource(overlaySource)
    }

    /** Show given map data with each the given style */
    @UiThread fun set(features: Collection<StyledElement>) {
        val mapLibreFeatures = features.flatMap { it.toFeatures() }
        overlaySource.setGeoJson(FeatureCollection.fromFeatures(mapLibreFeatures))
    }

    private fun StyledElement.toFeatures(): List<Feature> {
        val p = JsonObject()
        p.addProperty(ELEMENT_ID, element.id)
        p.addProperty(ELEMENT_TYPE, element.type.name)

        return when (style) {
            is PointStyle -> {
                if (style.icon != null) p.addProperty("icon", style.icon)
                if (style.label != null) p.addProperty("label", style.label)

                listOf(Feature.fromGeometry(geometry.center.toPoint(), p))
            }
            is PolygonStyle -> {
                if (style.color != INVISIBLE) {
                    p.addProperty("color", style.color)
                    p.addProperty("outline-color", getDarkenedColor(style.color))
                    p.addProperty("opacity", 0.8f)
                } else {
                    p.addProperty("opacity", 0f)
                }

                if (style.height != null) {
                    p.addProperty("height", style.height)
                }

                val f = Feature.fromGeometry(geometry.toMapLibreGeometry(), p)
                val label = if (style.label != null) {
                    Feature.fromGeometry(
                        geometry.center.toPoint(),
                        JsonObject().apply { addProperty("label", style.label) }
                    )
                } else null

                listOfNotNull(f, label)
            }
            is PolylineStyle -> {
                val line = geometry.toMapLibreGeometry()
                val width = getLineWidth(element.tags)
                if (isBridge(element.tags)) p.addProperty("bridge", true)

                val left = style.strokeLeft?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", 6f)
                    p2.addProperty("offset", -(width / 2f + 3f))
                    if (it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                    } else {
                        p2.addProperty("opacity", 0f)
                    }
                    if (it.dashed) p2.addProperty("dashed", true)
                    Feature.fromGeometry(line, p2)
                }

                val right = style.strokeRight?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", 6f)
                    p2.addProperty("offset", +(width / 2f + 3f))
                    if (it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                    } else {
                        p2.addProperty("opacity", 0f)
                    }
                    if (it.dashed) p2.addProperty("dashed", true)
                    Feature.fromGeometry(line, p2)
                }

                val center = style.stroke.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", width)
                    if (it != null && it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                        p2.addProperty("outline-color", getDarkenedColor(it.color))
                    } else {
                        p2.addProperty("opacity", 0f)
                    }
                    if (it?.dashed == true) p2.addProperty("dashed", true)
                    Feature.fromGeometry(line, p2)
                }

                val label = if (style.label != null) {
                    Feature.fromGeometry(
                        geometry.center.toPoint(),
                        JsonObject().apply { addProperty("label", style.label) }
                    )
                } else null

                listOfNotNull(left, right, center, label)
            }
        }
    }

    // no need to parse, modify and write to string darkening the same colors for every single element
    private fun getDarkenedColor(color: String): String =
        darkenedColors.getOrPut(color) {
            val rgb = color.toRGB()
            val hsv = rgb.toHsv()
            val darkenedHsv = hsv.copy(value = hsv.value * 2 / 3)
            darkenedHsv.toRgb().toHexString()
        }

    /** Clear map data */
    @UiThread fun clear() {
        overlaySource.clear()
    }

    companion object {
        private const val SOURCE = "overlay-source"
    }
}

private const val ELEMENT_TYPE = "element_type"
private const val ELEMENT_ID = "element_id"

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val style: Style
)

fun JsonElement.toElementKey(): ElementKey? {
    val id = asJsonObject.getAsJsonPrimitive(ELEMENT_ID)?.asLong ?: return null
    val type = asJsonObject.getAsJsonPrimitive(ELEMENT_TYPE).asString
    return if (type in ElementType.entries.map { it.toString() })
        ElementKey(ElementType.valueOf(type), id)
    else null
}

/** mimics width of line as seen in StreetComplete map style (or otherwise 3m) */
private fun getLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
    "motorway" -> 16f
    "motorway_link" -> 8f
    "trunk", "primary", "secondary", "tertiary" -> 12f
    "service", "track", "busway" -> 6f
    "path", "cycleway", "footway", "bridleway", "steps" -> 1f
    null -> 4f
    else -> 8f
}

private fun isBridge(tags: Map<String, String>): Boolean =
    tags["bridge"] != null && tags["bridge"] != "no"
