package de.westnordost.streetcomplete.screens.main.map.components

import com.mapzen.tangram.MapData
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.layers.PointStyle
import de.westnordost.streetcomplete.layers.PolygonStyle
import de.westnordost.streetcomplete.layers.PolylineStyle
import de.westnordost.streetcomplete.layers.Style
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.screens.main.map.tangram.toTangramGeometry

/** Takes care of displaying styled map data */
class StyleableLayerMapComponent(ctrl: KtMapController) {

    private val layer: MapData = ctrl.addDataLayer(MAP_DATA_LAYER)

    /** Shows/hides the map data */
    var isVisible: Boolean
        get() = layer.visible
        set(value) { layer.visible = value }

    /** Show given map data with each the given style */
    fun set(features: Collection<StyledElement>) {
        layer.setFeatures(features.flatMap { feature ->
            val props = HashMap<String, String>()
            props["element_id"] = feature.element.id.toString()
            props["element_type"] = feature.element.type.name
            when (feature.style) {
                is PolygonStyle -> {
                    getHeight(feature.element.tags)?.let { props["height"] = it.toString() }
                    props["color"] = feature.style.color
                    props["strokeColor"] = feature.style.strokeColor ?: feature.style.color
                    feature.style.label?.let { props["text"] = it }
                }
                is PolylineStyle -> {
                    props["width"] = getLineWidth(feature.element.tags).toString()
                    feature.style.stroke?.color?.let { props["color"] = it }
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

    /** Clear map data */
    fun clear() {
        layer.clear()
    }

    companion object {
        private const val MAP_DATA_LAYER = "streetcomplete_map_data"
    }
}

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val style: Style
)
