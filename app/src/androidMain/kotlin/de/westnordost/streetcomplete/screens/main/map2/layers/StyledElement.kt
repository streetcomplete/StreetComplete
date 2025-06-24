package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.screens.main.map2.toGeometry
import io.github.dellisd.spatialk.geojson.Feature
import kotlinx.serialization.json.JsonPrimitive

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val overlayStyle: OverlayStyle
)

private fun StyledElement.toGeoJsonFeatures(): List<Feature> {
        val p = createProperties(element.key)

        return when (overlayStyle) {
            is OverlayStyle.Point -> {
                if (overlayStyle.icon != null) {
                    p["icon"] = context.resources.getResourceEntryName(overlayStyle.icon)
                }
                if (overlayStyle.label != null) {
                    p["label"] = JsonPrimitive(overlayStyle.label)
                }

                listOf(Feature(geometry.center.toGeometry(), p))
            }
            is OverlayStyle.Polygon -> {
                if (overlayStyle.color.alpha != 0f) {
                    p["color"] = JsonPrimitive(overlayStyle.color.toRgbaString())
                    p["outline-color"] =  JsonPrimitive(overlayStyle.color.darkened().toRgbaString())
                    p["opacity"] = JsonPrimitive(0.8f)
                } else {
                    p["opacity"] = JsonPrimitive(0f)
                }

                if (overlayStyle.height != null && overlayStyle.color.alpha != 0f) {
                    p["height"] = JsonPrimitive(overlayStyle.height)
                    if (overlayStyle.minHeight != null) {
                        p["min-height"] = JsonPrimitive(overlayStyle.minHeight.coerceAtMost(overlayStyle.height))
                    }
                }

                val f = Feature(geometry.toGeometry(), p)
                val point = if (overlayStyle.label != null || overlayStyle.icon != null) {
                    val pp = createProperties(element.key)
                    if (overlayStyle.icon != null) {
                        pp["icon"] = context.resources.getResourceEntryName(overlayStyle.icon)
                    }
                    if (overlayStyle.label != null) {
                        pp["label"] = JsonPrimitive(overlayStyle.label)
                    }
                    Feature(geometry.center.toGeometry(), pp)
                } else {
                    null
                }

                listOfNotNull(f, point)
            }
            is OverlayStyle.Polyline -> {
                val line = geometry.toGeometry()
                val width = getLineWidth(element.tags)
                if (isBridge(element.tags)) {
                    p["bridge"] = JsonPrimitive(true)
                }

                val left = overlayStyle.strokeLeft?.let {
                    val p2 = HashMap(p)
                    p2["width"] = JsonPrimitive(3f)
                    p2["offset"] = JsonPrimitive(-(width / 2f + 1.5f))
                    if (it.color.alpha != 0f) {
                        p2["color"] = JsonPrimitive(it.color.toRgbaString())
                    } else {
                        p2["opacity"] = JsonPrimitive(0f)
                    }
                    if (it.dashed) {
                        p2["dashed"] = JsonPrimitive(true)
                    }
                    Feature(line, p2)
                }

                val right = overlayStyle.strokeRight?.let {
                    val p2 = HashMap(p)
                    p2["width"] = JsonPrimitive(3f)
                    p2["offset"] = JsonPrimitive(-(width / 2f + 1.5f))
                    if (it.color.alpha != 0f) {
                        p2["color"] = JsonPrimitive(it.color.toRgbaString())
                    } else {
                        p2["opacity"] = JsonPrimitive(0f)
                    }
                    if (it.dashed) {
                        p2["dashed"] = JsonPrimitive(true)
                    }
                    Feature(line, p2)
                }

                val center = overlayStyle.stroke.let {
                    val p2 = HashMap(p)
                    p2["width"] = JsonPrimitive(width)
                    if (it != null && it.color.alpha != 0f) {
                        p2["color"] = JsonPrimitive(it.color.toRgbaString())
                        p2["outline-color"] = JsonPrimitive(it.color.darkened().toRgbaString())
                    } else {
                        p2["opacity"] = JsonPrimitive(0f)
                    }
                    if (it?.dashed == true) {
                        p2["dashed"] = JsonPrimitive(true)
                    }
                    Feature(line, p2)
                }

                val label = if (overlayStyle.label != null) {
                    Feature(
                        geometry.center.toGeometry(),
                        mapOf("label" to JsonPrimitive(overlayStyle.label))
                    )
                } else {
                    null
                }

                listOfNotNull(left, right, center, label)
            }
        }
    }

private fun createProperties(key: ElementKey): MutableMap<String, JsonPrimitive> {
    val p = HashMap<String, JsonPrimitive>()
    p[ELEMENT_ID] = JsonPrimitive(key.id)
    p[ELEMENT_TYPE] = JsonPrimitive(key.type.name)
    return p
}

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

private fun OverlayStyle.getIcon(): Int? = when (this) {
    is OverlayStyle.Point -> icon
    is OverlayStyle.Polygon -> icon
    is OverlayStyle.Polyline -> null
}

private fun Color.darkened(): Color = Color(
    red = red * 0.67f,
    green = green * 0.67f,
    blue = blue * 0.67f,
    alpha = alpha
)

private fun Color.toRgbaString(): String {
    val c = toArgb()
    return "rgba(${(c shr 16) and 0xFF}, ${(c shr 8) and 0xFF}, ${c and 0xFF}, ${alpha})"
}

private const val ELEMENT_TYPE = "element_type"
private const val ELEMENT_ID = "element_id"
