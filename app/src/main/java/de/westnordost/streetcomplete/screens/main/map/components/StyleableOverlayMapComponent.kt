package de.westnordost.streetcomplete.screens.main.map.components

import androidx.annotation.UiThread
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
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
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.addTransparency
import de.westnordost.streetcomplete.util.ktx.darken
import de.westnordost.streetcomplete.util.ktx.toARGBString
import de.westnordost.streetcomplete.util.ktx.toColorInt

/** Takes care of displaying styled map data */
class StyleableOverlayMapComponent(private val map: MapboxMap) {

    private val overlaySource = GeoJsonSource(
        SOURCE,
        GeoJsonOptions().withMinZoom(16)
    )

    private val darkenedColors = HashMap<String, String>()
    private val transparentColors = HashMap<String, String>()

    val layers: List<Layer> = listOf(
        LineLayer("overlay-lines-casing", SOURCE)
            .withFilter(all(
                eq(get("type"), "line"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("outline-color")),
                lineOpacity(get("opacity")),
                lineOffset(changeDistanceWithZoom("offset")),
                lineWidth(changeDistanceWithZoom("width")),
            ),
        FillLayer("overlay-fills", SOURCE)
            .withFilter(all(
                eq(get("type"), "polygon"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                fillColor(get("color")),
                fillOpacity(get("opacity")),
            ),
        LineLayer("overlay-dashed-lines", SOURCE)
            .withFilter(all(
                eq(get("type"), "line"),
                has("dashed"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                lineOffset(changeDistanceWithZoom("offset")),
                lineWidth(changeDistanceWithZoom("width")),
                lineDasharray(arrayOf(1.5f, 1f)),
            ),
        LineLayer("overlay-lines", SOURCE)
            .withFilter(all(
                eq(get("type"), "line"),
                not(has("dashed")),
                gte(zoom(), 16f)
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                lineOffset(changeDistanceWithZoom("offset")),
                lineWidth(changeDistanceWithZoom("width")),
            ),
        LineLayer("overlay-fills-outline", SOURCE)
            .withFilter(all(
                eq(get("type"), "polygon"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("outline-color")),
                lineOpacity(get("opacity")),
                lineWidth(4f),
            ),
        FillExtrusionLayer("overlay-heights", SOURCE)
            .withFilter(all(
                eq(get("type"), "polygon"),
                has("height"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                fillExtrusionColor(get("color")),
                fillExtrusionOpacity(get("opacity")),
                fillExtrusionHeight(get("height")),
            ),
        SymbolLayer("overlay-symbols", SOURCE)
            .withFilter(gte(zoom(), 16f))
            .withProperties(
                iconImage("{icon}"),
                textField("{label}"),
                // or maybe read text properties from feature?
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
                textHaloColor("white"),
                textHaloWidth(1.5f),
                iconColor("black"),
                iconHaloColor("white"),
                iconHaloWidth(1.5f),
                iconHaloBlur(2f),
                iconAllowOverlap(step(zoom(), literal(false), stop(18, true))),
                textAllowOverlap(step(zoom(), literal(false), stop(18, true))),
            )
    )

    /** Shows/hides the map data */
    var isVisible: Boolean
        // add / remove source
        @UiThread get() =
            map.style?.sources?.find { it.id == "overlay-source" } != null
        @UiThread set(value) {
            if (isVisible == value) return
            if (value) {
                map.style?.addSource(overlaySource)
            } else {
                map.style?.removeSource(overlaySource)
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
                p.addProperty("type", "polygon")
                if (style.color != INVISIBLE) {
                    p.addProperty("color", style.color)
                    p.addProperty("outline-color", getDarkenedColor(style.color))
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
                p.addProperty("type", "line")

                val line = geometry.toMapLibreGeometry()
                val width = getLineWidth(element.tags)

                val left = style.strokeLeft?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", 4f)
                    p2.addProperty("offset", -width - 2f)
                    if (it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                        p2.addProperty("outline-color", getDarkenedColor(it.color))
                    }
                    if (it.dashed) p2.addProperty("dashed", true)
                    Feature.fromGeometry(line, p2)
                }

                val right = style.strokeRight?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", 4f)
                    p2.addProperty("offset", +width + 2f)
                    if (it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                        p2.addProperty("outline-color", getDarkenedColor(it.color))
                    }
                    if (it.dashed) p2.addProperty("dashed", true)
                    Feature.fromGeometry(line, p2)
                }

                val center = style.stroke?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", width)
                    p2.addProperty("color", it.color)
                    if (it.color != INVISIBLE) {
                        p2.addProperty("color", it.color)
                        p2.addProperty("outline-color", getDarkenedColor(it.color))
                    }
                    if (it.dashed) p2.addProperty("dashed", true)
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

    /** mimics width of line as seen in StreetComplete map style (or otherwise 3m) */
    private fun getLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
        "motorway" -> 15f
        "motorway_link" -> 4.5f
        "trunk", "primary", "secondary", "tertiary" -> 7.5f
        "service", "track" -> 3f
        "path", "cycleway", "footway", "bridleway", "steps" -> 1f
        null -> 3f
        else -> 5.5f
    }

    // no need to parse, modify and write to string darkening the same colors for every single element
    private fun getDarkenedColor(color: String): String =
        darkenedColors.getOrPut(color) { toARGBString(darken(toColorInt(color), 0.67f)) }

    private fun getColorWithSomeTransparency(color: String): String =
        transparentColors.getOrPut(color) { toARGBString(addTransparency(toColorInt(color), 0.6f)) }

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
