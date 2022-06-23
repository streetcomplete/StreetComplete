package de.westnordost.streetcomplete.screens.main.map.components

import android.content.res.Resources
import android.graphics.Color
import com.mapzen.tangram.MapData
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.util.ktx.darken
import de.westnordost.streetcomplete.util.ktx.toARGBString

/** Takes care of displaying styled map data */
class StyleableOverlayMapComponent(private val resources: Resources, ctrl: KtMapController) {

    private val layer: MapData = ctrl.addDataLayer(MAP_DATA_LAYER)

    private val darkenedColors = HashMap<String, String>()

    /** Shows/hides the map data */
    var isVisible: Boolean
        get() = layer.visible
        set(value) { layer.visible = value }

    /** Show given map data with each the given style */
    fun set(features: Collection<StyledElement>) {
        layer.setFeatures(features.flatMap { (element, geometry, style) ->
            val props = ArrayList<Pair<String, String>>()
            props.add(ELEMENT_ID to element.id.toString())
            props.add(ELEMENT_TYPE to element.type.name)
            when (style) {
                is PolygonStyle -> {
                    getHeight(element.tags)?.let { props.add("height" to it.toString()) }
                    props.add("color" to style.color)
                    props.add("strokeColor" to getDarkenedColor(style.color))
                    style.label?.let { props.add("text" to it) }
                }
                is PolylineStyle -> {
                    props.add("width" to getLineWidth(element.tags).toString())
                    style.colorLeft?.let { props.add("colorLeft" to it) }
                    style.colorRight?.let { props.add("colorRight" to it) }
                    if (style.color != null) {
                        props.add("color" to style.color)
                        props.add("strokeColor" to getDarkenedColor(style.color))
                    } else if (style.colorLeft != null || style.colorRight != null) {
                        // must have a color for the center if left or right is defined because
                        // there are really ugly overlaps in tangram otherwise
                        props.add("color" to resources.getString(R.string.road_color))
                        props.add("strokeColor" to resources.getString(R.string.road_outline_color))
                    }
                    style.label?.let { props.add("text" to it) }
                }
                is PointStyle -> {
                    style.label?.let { props.add("text" to it) }
                }
            }

            geometry.toTangramGeometry(props)
        })
    }

    /** mimics width of line as seen in Streetomplete map style (or otherwise 3m) */
    private fun getLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
        "motorway", "trunk" -> 20f
        "primary", "secondary" -> 12f
        "service", "track" -> 4f
        "path", "cycleway", "footway", "bridleway", "steps" -> 2f
        null -> 3f
        else -> 8f
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
