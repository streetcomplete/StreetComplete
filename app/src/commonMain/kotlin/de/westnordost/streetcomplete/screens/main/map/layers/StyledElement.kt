package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val style: OverlayStyle
)

fun StyledElement.toGeoJsonFeatures(): List<Feature<Geometry, JsonObject>> {
    val p = createProperties(element.key, style.disabled)

    return when (style) {
        is OverlayStyle.Point -> {
            if (style.icon != null) {
                p["icon"] = JsonPrimitive(style.icon.id)
            }
            if (style.label != null) {
                p["label"] = JsonPrimitive(style.label)
            }

            listOf(Feature(geometry.center.toGeometry(), JsonObject(p)))
        }
        is OverlayStyle.Polygon -> {
            if (style.color.alpha != 0f) {
                p["color"] = JsonPrimitive(style.color.toRgbaString())
                p["outline-color"] =  JsonPrimitive(style.color.darkened().toRgbaString())
                p["opacity"] = JsonPrimitive(0.8f)
            } else {
                p["opacity"] = JsonPrimitive(0f)
            }

            if (style.height != null && style.color.alpha != 0f) {
                p["height"] = JsonPrimitive(style.height)
                if (style.minHeight != null) {
                    p["min-height"] = JsonPrimitive(style.minHeight.coerceAtMost(style.height))
                }
            }

            val f = Feature(geometry.toGeometry(), JsonObject(p))
            val point = if (style.label != null || style.icon != null) {
                val pp = createProperties(element.key, style.disabled)
                if (style.icon != null) {
                    pp["icon"] = JsonPrimitive(style.icon.id)
                }
                if (style.label != null) {
                    pp["label"] = JsonPrimitive(style.label)
                }
                Feature(geometry.center.toGeometry(), JsonObject(pp))
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

            val left = style.strokeLeft?.let {
                val p2 = HashMap(p)
                p2["width"] = JsonPrimitive(3f)
                p2["offset"] = JsonPrimitive(-(width / 2f + 1.5f))
                if (it.color.alpha != 0f) {
                    p2["color"] = JsonPrimitive(it.color.toRgbaString())
                    p2["opacity"] = JsonPrimitive(1f)
                } else {
                    p2["opacity"] = JsonPrimitive(0f)
                }
                if (it.dashed) {
                    p2["dashed"] = JsonPrimitive(true)
                }
                Feature(line, JsonObject(p2))
            }

            val right = style.strokeRight?.let {
                val p2 = HashMap(p)
                p2["width"] = JsonPrimitive(3f)
                p2["offset"] = JsonPrimitive(+(width / 2f + 1.5f))
                if (it.color.alpha != 0f) {
                    p2["color"] = JsonPrimitive(it.color.toRgbaString())
                    p2["opacity"] = JsonPrimitive(1f)
                } else {
                    p2["opacity"] = JsonPrimitive(0f)
                }
                if (it.dashed) {
                    p2["dashed"] = JsonPrimitive(true)
                }
                Feature(line, JsonObject(p2))
            }

            val center = style.stroke.let {
                val p2 = HashMap(p)
                p2["width"] = JsonPrimitive(width)
                if (it != null && it.color.alpha != 0f) {
                    p2["color"] = JsonPrimitive(it.color.toRgbaString())
                    p2["outline-color"] = JsonPrimitive(it.color.darkened().toRgbaString())
                    p2["opacity"] = JsonPrimitive(1f)
                } else {
                    p2["opacity"] = JsonPrimitive(0f)
                }
                if (it?.dashed == true) {
                    p2["dashed"] = JsonPrimitive(true)
                }
                Feature(line, JsonObject(p2))
            }

            val label = if (style.label != null) {
                Feature(
                    geometry.center.toGeometry(),
                    JsonObject(mapOf("label" to JsonPrimitive(style.label)))
                )
            } else {
                null
            }

            listOfNotNull(left, right, center, label)
        }
    }
}

fun JsonObject.getElementKey(): ElementKey? {
    val id = get(ELEMENT_ID)?.jsonPrimitive?.longOrNull ?: return null
    val type = get(ELEMENT_TYPE)?.jsonPrimitive?.content ?: return null
    return ElementKey(ElementType.valueOf(type), id)
}
fun JsonObject.isDisabled(): Boolean =
    get(DISABLED)?.jsonPrimitive?.booleanOrNull ?: false

private fun createProperties(key: ElementKey, disabled: Boolean): MutableMap<String, JsonPrimitive> {
    val p = HashMap<String, JsonPrimitive>()
    p[ELEMENT_ID] = JsonPrimitive(key.id)
    p[ELEMENT_TYPE] = JsonPrimitive(key.type.name)
    if (disabled) p[DISABLED] = JsonPrimitive(disabled)
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

private fun OverlayStyle.getIcon(): DrawableResource? = when (this) {
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
private const val DISABLED = "disabled"
