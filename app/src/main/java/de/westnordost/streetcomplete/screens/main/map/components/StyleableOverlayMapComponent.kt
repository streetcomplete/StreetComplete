package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import android.graphics.Color
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Geometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
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
        set(value) { layer.visible = value }

    /** Show given map data with each the given style */
    fun set(features: Collection<StyledElement>) {
        layer.setFeatures(features.flatMap { styledElement ->
            styledElement.tangramGeometries?.let { return@flatMap it }

            val geometries = createTangramGeometries(styledElement)
            styledElement.tangramGeometries = geometries
            geometries
        })
    }

    private fun createTangramGeometries(styledElement: StyledElement): List<Geometry> {
        val element = styledElement.element
        val geometry = styledElement.geometry
        val style = styledElement.style
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

        return if (style is PolygonStyle && (style.icon != null || style.label != null)) {
            // workaround for https://github.com/tangrams/tangram-es/issues/2332 and an unreported
            // issue that icons for polygons are shown on every single vertex
            val properties = HashMap<String, String>(4, 1.0f)
            properties[ELEMENT_ID] = element.id.toString()
            properties[ELEMENT_TYPE] = element.type.name
            style.icon?.let { properties["icon"] = it }
            style.label?.let { properties["text"] = it }
            geometry.toTangramGeometry(props) + ElementPointGeometry(geometry.center).toTangramGeometry(properties)
        } else {
            geometry.toTangramGeometry(props)
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
) {
    // geometries may contain road color, which depends on current theme
    // however, storing is not an issue as styled elements are cleared on theme switch (both automatic and manual)
    var tangramGeometries: List<Geometry>? = null
}
