package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import android.graphics.Color
import com.google.gson.JsonElement
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapzen.tangram.MapData
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.screens.main.map.toJsonProperties
import de.westnordost.streetcomplete.util.ktx.addTransparency
import de.westnordost.streetcomplete.util.ktx.darken
import de.westnordost.streetcomplete.util.ktx.toARGBString
import kotlin.math.absoluteValue

/** Takes care of displaying styled map data */
class StyleableOverlayMapComponent(private val resources: Resources, ctrl: KtMapController) {

    private val layer: MapData = ctrl.addDataLayer(MAP_DATA_LAYER)

    private val darkenedColors = HashMap<String, String>()
    private val transparentColors = HashMap<String, String>()

    /** Shows/hides the map data */
    var isVisible: Boolean
        get() = layer.visible
        set(value) {
            layer.visible = value
            if (!value) {
                MainMapFragment.overlaySymbolManager!!.deleteAll()
                MainMapFragment.overlayFillManager!!.deleteAll()
                MainMapFragment.overlayLineManager!!.deleteAll()
            }
        }

    /** Show given map data with each the given style */
    fun set(features: Collection<StyledElement>) {
        layer.setFeatures(features.flatMap { (element, geometry, style) ->
            val props = HashMap<String, String>()
            props[ELEMENT_ID] = element.id.toString()
            props[ELEMENT_TYPE] = element.type.name
            val layer = element.tags["layer"]?.toIntOrNull()?.takeIf { it.absoluteValue <= 20 } ?: 0
            props["layer"] = layer.toString()
            when (style) {
                is PolygonStyle -> {
                    getHeight(element.tags)?.let { props["height"] = it.toString() }
                    props["color"] = getColorWithSomeTransparency(style.color)
                    props["strokeColor"] = getColorWithSomeTransparency(getDarkenedColor(style.color))
                }
                is PolylineStyle -> {
                    val width = getLineWidth(element.tags)
                    // thin lines should be rendered on top (see #4291)
                    if (width <= 2f) props["layer"] = (layer + 1).toString()
                    props["width"] = width.toString()
                    style.strokeLeft?.let {
                        if (it.dashed) props["dashedLeft"] = "1"
                        props["colorLeft"] = it.color
                    }
                    style.strokeRight?.let {
                        if (it.dashed) props["dashedRight"] = "1"
                        props["colorRight"] = it.color
                    }
                    if (style.stroke != null) {
                        if (style.stroke.dashed) props["dashed"] = "1"
                        props["color"] = style.stroke.color
                        props["strokeColor"] = getDarkenedColor(style.stroke.color)
                    } else if (style.strokeLeft != null || style.strokeRight != null) {
                        // must have a color for the center if left or right is defined because
                        // there are really ugly overlaps in tangram otherwise
                        props["color"] = resources.getString(R.string.road_color)
                        props["strokeColor"] = resources.getString(R.string.road_outline_color)
                    }
                    style.label?.let { props["text"] = it }
                }
                is PointStyle -> {
                    style.label?.let { props["text"] = it }
                    style.icon?.let { props["icon"] = it }
                }
            }

            geometry.toTangramGeometry(props)
        } + // workaround for https://github.com/tangrams/tangram-es/issues/2332 and an unreported
            // issue that icons for polygons are shown on every single vertex
            features
            .filter { it.style is PolygonStyle && (it.style.icon != null || it.style.label != null) }
            .flatMap { (element, geometry, style) ->
                val props = HashMap<String, String>(4)
                val polygonStyle = style as PolygonStyle
                props[ELEMENT_ID] = element.id.toString()
                props[ELEMENT_TYPE] = element.type.name
                polygonStyle.icon?.let { props["icon"] = it }
                polygonStyle.label?.let { props["text"] = it }
                ElementPointGeometry(geometry.center).toTangramGeometry(props)
            }
        )
        val annotations = features.flatMap { (element, geometry, style) ->
            // probably there should be separate line/fill/circle/symbol managers for overlays
            val data = listOf(
                ELEMENT_ID to element.id.toString(),
                ELEMENT_TYPE to element.type.name
            ).toJsonProperties()
            when (style) {
                is PointStyle -> {
                    val o = SymbolOptions()
                        .withData(data)
                        .withLatLng(geometry.center.toLatLng())
                    // currently one of those is not null, so we need a symbol anyway
                    if (style.icon != null) o.withIconImage(style.icon.toString())
                    if (style.label != null) o.withTextField(style.label)
                    listOf(o)
                }
                is PolygonStyle -> {
                    val o = FillOptions()
                        .withData(data)
                        .withFillColor(style.color)
                        .withFillOutlineColor(getDarkenedColor(style.color))
                        .withLatLngs((geometry as ElementPolygonsGeometry).polygons.map { it.map { it.toLatLng() } })
                    listOf(o)
                    // no label here...
                }
                is PolylineStyle -> {
                    (geometry as ElementPolylinesGeometry).polylines.flatMap { line ->
                        val left = if (style.strokeLeft != null) {
                            LineOptions()
                                .withData(data)
                                .withLatLngs(line.map { it.toLatLng() })
                                .withLineWidth(10f) // todo...
                                .withLineOffset(15f)
                                .withLineColor(style.strokeLeft.color)
                            //.withLinePattern() // how to set dashed style?
                        } else null
                        val right = if (style.strokeRight != null) {
                            LineOptions()
                                .withData(data)
                                .withLatLngs(line.map { it.toLatLng() })
                                .withLineWidth(10f) // todo...
                                .withLineOffset(-15f)
                                .withLineColor(style.strokeRight.color)
                            //.withLinePattern() // how to set dashed style?
                        } else null
                        val center = if (style.stroke != null && style.stroke.color != de.westnordost.streetcomplete.overlays.Color.INVISIBLE) {
                            LineOptions()
                                .withData(data)
                                .withLatLngs(line.map { it.toLatLng() })
                                .withLineWidth(getLineWidth(element.tags) * 1.5f) // 1.5 is pixel density, depends on phone
                                .withLineColor(style.stroke.color)
                            //.withLinePattern() // how to set dashed style?
                        } else null
                        listOfNotNull(left, right, center)
                        // no label here...
                    }
                }
            }
        }
        MainActivity.activity?.runOnUiThread {
            MainMapFragment.overlaySymbolManager!!.deleteAll()
            MainMapFragment.overlayFillManager!!.deleteAll()
            MainMapFragment.overlayLineManager!!.deleteAll()
            MainMapFragment.overlaySymbolManager!!.create(annotations.filterIsInstance<SymbolOptions>())
            MainMapFragment.overlayFillManager!!.create(annotations.filterIsInstance<FillOptions>())
            MainMapFragment.overlayLineManager!!.create(annotations.filterIsInstance<LineOptions>())
        }
    }

    /** mimics width of line as seen in StreetComplete map style (or otherwise 3m) */
    private fun getLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
        "motorway" -> if (!isOneway(tags)) 15f else 7.5f
        "motorway_link" -> 4.5f
        "trunk", "primary", "secondary", "tertiary" -> if (!isOneway(tags)) 7.5f else 4.5f
        "service", "track" -> 3f
        "path", "cycleway", "footway", "bridleway", "steps" -> 1f
        null -> 3f
        else -> if (!isOneway(tags)) 5.5f else 3f
    }

    /** estimates height of thing */
    private fun getHeight(tags: Map<String, String>): Float? {
        val height = tags["height"]?.toFloatOrNull()
        if (height != null) return height
        val buildingLevels = tags["building:levels"]?.toFloatOrNull()
        val roofLevels = tags["roof:levels"]?.toFloatOrNull()
        if (buildingLevels != null) return 3f * (buildingLevels + (roofLevels ?: 0f))
        return null
    }

    // no need to parse, modify and write to string darkening the same colors for every single element
    private fun getDarkenedColor(color: String): String =
        darkenedColors.getOrPut(color) { toARGBString(darken(Color.parseColor(color), 0.67f)) }

    private fun getColorWithSomeTransparency(color: String): String =
        // alpha is actually double of what is specified https://github.com/tangrams/tangram-es/issues/2333
        transparentColors.getOrPut(color) { toARGBString(addTransparency(Color.parseColor(color), 0.6f)) }

    /** Clear map data */
    fun clear() {
        layer.clear()
    }

    fun getElementKey(properties: Map<String, String>): ElementKey? {
        val type = properties[ELEMENT_TYPE]?.let { ElementType.valueOf(it) } ?: return null
        val id = properties[ELEMENT_ID]?.toLong() ?: return null
        return ElementKey(type, id)
    }

    companion object {
        private const val MAP_DATA_LAYER = "streetcomplete_map_data"
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
    // todo: what are the values if it doesn't exist?
    val id = asJsonObject.getAsJsonPrimitive(ELEMENT_ID).asString.toLongOrNull() ?: return null
    val type = asJsonObject.getAsJsonPrimitive(ELEMENT_TYPE).asString
    return if (type in ElementType.values().map { it.toString() })
        ElementKey(ElementType.valueOf(type), id)
    else null
}
