package de.westnordost.streetcomplete.screens.main.map.components

import android.graphics.Color
import com.mapzen.tangram.MapData
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.layers.PointStyle
import de.westnordost.streetcomplete.layers.PolygonStyle
import de.westnordost.streetcomplete.layers.PolylineStyle
import de.westnordost.streetcomplete.layers.Style
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.util.ktx.darken
import de.westnordost.streetcomplete.util.ktx.toARGBString

/** Takes care of displaying styled map data */
class StyleableLayerMapComponent(ctrl: KtMapController) {

    private val layer: MapData = ctrl.addDataLayer(MAP_DATA_LAYER)

    private val darkenedColors = HashMap<String, String>()

    /** Shows/hides the map data */
    var isVisible: Boolean
        get() = layer.visible
        set(value) { layer.visible = value }

    /** Show given map data with each the given style */
    fun set(features: Collection<StyledElement>) {
        layer.setFeatures(features.flatMap { feature ->
            val props = HashMap<String, String>()
            props[ELEMENT_ID] = feature.element.id.toString()
            props[ELEMENT_TYPE] = feature.element.type.name
            when (feature.style) {
                is PolygonStyle -> {
                    getHeight(feature.element.tags)?.let { props["height"] = it.toString() }
                    props["color"] = feature.style.color
                    props["strokeColor"] = getDarkenedColor(feature.style.color)
                    feature.style.label?.let { props["text"] = it }
                }
                is PolylineStyle -> {
                    props["width"] = getLineWidth(feature.element.tags).toString()
                    feature.style.stroke?.color?.let {
                        props["color"] = it
                        props["strokeColor"] = getDarkenedColor(it)
                    }
                    feature.style.strokeLeft?.color?.let { props["colorLeft"] = it }
                    feature.style.strokeRight?.color?.let { props["colorRight"] = it }
                    feature.style.label?.let { props["text"] = it }
                }
                is PointStyle -> {
                    feature.style.label?.let { props["text"] = it }
                }
            }

            feature.geometry.toTangramGeometry(props)
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
        if (buildingLevels != null) return buildingLevels + (roofLevels ?: 0f)
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
