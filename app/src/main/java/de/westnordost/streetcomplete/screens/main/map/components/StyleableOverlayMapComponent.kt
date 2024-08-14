package de.westnordost.streetcomplete.screens.main.map.components

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.UiThread
import com.google.gson.JsonObject
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
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
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.overlays.Color.INVISIBLE
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.screens.main.map.createIconBitmap
import de.westnordost.streetcomplete.screens.main.map.maplibre.MapImages
import de.westnordost.streetcomplete.screens.main.map.maplibre.inMeters
import de.westnordost.streetcomplete.screens.main.map.maplibre.clear
import de.westnordost.streetcomplete.screens.main.map.maplibre.isArea
import de.westnordost.streetcomplete.screens.main.map.maplibre.isLine
import de.westnordost.streetcomplete.screens.main.map.maplibre.isPoint
import de.westnordost.streetcomplete.screens.main.map.maplibre.queryRenderedFeatures
import de.westnordost.streetcomplete.screens.main.map.maplibre.toMapLibreGeometry
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPoint
import de.westnordost.streetcomplete.util.ktx.toRGB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

/** Takes care of displaying styled map data */
class StyleableOverlayMapComponent(
    private val context: Context,
    private val map: MapLibreMap,
    private val mapImages: MapImages,
    private val clickRadius: Float,
    private val onClickElement: (key: ElementKey) -> Unit
) {

    private val overlaySource = GeoJsonSource(
        SOURCE,
        GeoJsonOptions().withMinZoom(16)
    )

    private val darkenedColors = HashMap<String, String>()

    private val isNightMode: Boolean get() {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private val sideLinesProperties = arrayOf(
        lineCap(Property.LINE_CAP_BUTT),
        lineJoin(Property.LINE_JOIN_ROUND),
        lineColor(get("color")),
        lineOpacity(get("opacity")),
        lineOffset(inMeters(get("offset"))),
        lineWidth(inMeters(get("width"))),
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
                lineGapWidth(inMeters(get("width"))),
                lineWidth(inMeters(0.5f)),
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
                lineWidth(inMeters(get("width"))),
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
                lineWidth(inMeters(get("width"))),
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
                lineWidth(inMeters(0.5f)),
            ),
        FillExtrusionLayer("overlay-heights", SOURCE)
            .withFilter(all(
                isArea(),
                has("height"),
                gte(zoom(), 16f)
            ))
            .withProperties(
                fillExtrusionColor(get("color")),
                fillExtrusionOpacity(1f), // can't use get("opacity"), data expressions not supported
                fillExtrusionHeight(get("height")),
                fillExtrusionBase(get("min-height")),
            ),
    )

    val labelLayers = listOf(
        SymbolLayer("overlay-symbols", SOURCE)
            .withFilter(all(
                gte(zoom(), 17f),
                isPoint()
            ))
            .withProperties(
                iconImage(get("icon")),
                textField(get("label")),
                textFont(arrayOf("Roboto Regular")),
                textAnchor(Property.TEXT_ANCHOR_TOP),
                textOffset(switchCase(has("icon"), literal(arrayOf(0f, 1f)), literal(arrayOf(0f, 0f)))),
                textSize(16 * context.resources.configuration.fontScale),
                textColor(if (isNightMode) "#ccf" else "#124"),
                textHaloColor(if (isNightMode) "#2e2e48" else "#fff"),
                textHaloWidth(2.5f),
                iconColor(if (isNightMode) "#ccf" else "#124"),
                iconHaloColor(if (isNightMode) "#2e2e48" else "#fff"),
                iconHaloWidth(2.5f),
                textOptional(true),
                iconAllowOverlap(true),
                textAllowOverlap(step(zoom(), literal(false), stop(21, true))),
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE),
            ),
    )

    private val allLayers = layers + sideLayers + sideLayersBridge + labelLayers

    /** Shows/hides the map data */
    @UiThread fun setVisible(value: Boolean) {
        val visibility = if (value) Property.VISIBLE else Property.NONE
        allLayers.forEach { it.setProperties(visibility(visibility)) }
    }

    init {
        overlaySource.isVolatile = true
        map.style?.addSource(overlaySource)
        map.addOnMapClickListener(::onClick)
    }

    /** Show given map data with each the given style */
    suspend fun set(styledElements: Collection<StyledElement>) {
        val icons = styledElements.mapNotNull { it.style.getIcon() }
        mapImages.addOnce(icons) {
            val name = context.resources.getResourceEntryName(it)
            val sdf = name.startsWith("ic_preset_")
            createIconBitmap(context, it, sdf) to sdf
        }
        val features = styledElements.flatMap { it.toFeatures() }
        val mapLibreFeatures = FeatureCollection.fromFeatures(features)
        withContext(Dispatchers.Main) { overlaySource.setGeoJson(mapLibreFeatures) }
    }

    private fun onClick(position: LatLng): Boolean {
        val feature = map.queryRenderedFeatures(
            coordinates = map.projection.toScreenLocation(position),
            radius = clickRadius,
            layerIds = arrayOf("overlay-symbols", "overlay-lines", "overlay-lines-dashed", "overlay-fills")
        ).firstOrNull() ?: return false

        val properties = feature.properties() ?: return false

        val elementKey = getElementKey(properties)
        if (elementKey != null) {
            onClickElement(elementKey)
            return true
        }
        return false
    }

    private fun StyledElement.toFeatures(): List<Feature> {
        val p = getElementKeyProperties(element.key)

        return when (style) {
            is PointStyle -> {
                if (style.icon != null) {
                    p.addProperty("icon", context.resources.getResourceEntryName(style.icon))
                }
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

                if (style.height != null && style.color != INVISIBLE) {
                    p.addProperty("height", style.height)
                    if (style.minHeight != null) {
                        p.addProperty("min-height", style.minHeight.coerceAtMost(style.minHeight))
                    }
                }

                val f = Feature.fromGeometry(geometry.toMapLibreGeometry(), p)
                val point = if (style.label != null || style.icon != null) {
                    val pp = getElementKeyProperties(element.key)
                    if (style.icon != null) {
                        pp.addProperty("icon", context.resources.getResourceEntryName(style.icon))
                    }
                    if (style.label != null) pp.addProperty("label", style.label)
                    Feature.fromGeometry(geometry.center.toPoint(),pp)
                } else null

                listOfNotNull(f, point)
            }
            is PolylineStyle -> {
                val line = geometry.toMapLibreGeometry()
                val width = getLineWidth(element.tags)
                if (isBridge(element.tags)) p.addProperty("bridge", true)

                val left = style.strokeLeft?.let {
                    val p2 = p.deepCopy()
                    p2.addProperty("width", 3f)
                    p2.addProperty("offset", -(width / 2f + 1.5f))
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
                    p2.addProperty("width", 3f)
                    p2.addProperty("offset", +(width / 2f + 1.5f))
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

    private fun getElementKey(properties: JsonObject): ElementKey? {
        val id = properties[ELEMENT_ID]?.asLong ?: return null
        val type = properties[ELEMENT_TYPE]?.asString ?: return null
        return ElementKey(ElementType.valueOf(type), id)
    }

    private fun getElementKeyProperties(key: ElementKey): JsonObject {
        val p = JsonObject()
        p.addProperty(ELEMENT_ID, key.id)
        p.addProperty(ELEMENT_TYPE, key.type.name)
        return p
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

        private const val ELEMENT_TYPE = "element_type"
        private const val ELEMENT_ID = "element_id"
    }
}

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val style: Style
)

/** mimics width of line as seen in StreetComplete map style */
private fun getLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
    "motorway" -> 8f
    "motorway_link" -> 4f
    "trunk", "primary", "secondary", "tertiary" -> 6f
    "service", "track", "busway" -> 3f
    "path", "cycleway", "footway", "bridleway", "steps" -> 1.0f
    null -> 2f
    else -> 4f
}

private fun isBridge(tags: Map<String, String>): Boolean =
    tags["bridge"] != null && tags["bridge"] != "no"

private fun Style.getIcon(): Int? = when(this) {
    is PointStyle -> icon
    is PolygonStyle -> icon
    is PolylineStyle -> null
}
