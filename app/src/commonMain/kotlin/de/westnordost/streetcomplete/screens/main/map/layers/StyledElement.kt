package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point

data class StyledElement(
    val element: Element,
    val geometry: ElementGeometry,
    val style: OverlayStyle,
)

internal fun StyledElement.toOverlayFeatures(): List<Feature<Geometry, JsonObject>> {
    val properties = elementProperties(element.key, style.disabled)

    return when (style) {
        is OverlayStyle.Point -> listOf(
            Feature(
                Point(geometry.center.toPosition()),
                JsonObject(properties.applyPointStyle(style.icon, style.label)),
            )
        )

        is OverlayStyle.Polygon -> {
            if (style.color != OverlayColor.Invisible) {
                properties[COLOR] = JsonPrimitive(style.color.toRgbaString())
                properties[OUTLINE_COLOR] = JsonPrimitive(style.color.darkened().toRgbaString())
                properties[OPACITY] = JsonPrimitive(0.8f)
                style.height?.let { height ->
                    properties[HEIGHT] = JsonPrimitive(height)
                    style.minHeight?.let {
                        properties[MIN_HEIGHT] = JsonPrimitive(it.coerceAtMost(height))
                    }
                }
            } else {
                properties[OPACITY] = JsonPrimitive(0f)
            }

            val area = Feature(geometry.toGeometry(), JsonObject(properties))
            val center = if (style.label != null || style.icon != null) {
                Feature(
                    Point(geometry.center.toPosition()),
                    JsonObject(
                        elementProperties(element.key, style.disabled)
                            .applyPointStyle(style.icon, style.label)
                    ),
                )
            } else {
                null
            }
            listOfNotNull(area, center)
        }

        is OverlayStyle.Polyline -> {
            val line = geometry.toGeometry()
            val roadWidth = overlayLineWidth(element.tags)
            if (isOverlayBridge(element.tags)) properties[BRIDGE] = JsonPrimitive(true)

            val left = style.strokeLeft?.let { stroke ->
                Feature(
                    line,
                    JsonObject(
                        properties.toMutableMap().applyStroke(
                            stroke = stroke,
                            width = 3f,
                            offset = -(roadWidth / 2f + 1.5f),
                        )
                    ),
                )
            }
            val right = style.strokeRight?.let { stroke ->
                Feature(
                    line,
                    JsonObject(
                        properties.toMutableMap().applyStroke(
                            stroke = stroke,
                            width = 3f,
                            offset = roadWidth / 2f + 1.5f,
                        )
                    ),
                )
            }

            val centerProperties = properties.toMutableMap().apply {
                this[WIDTH] = JsonPrimitive(roadWidth)
                val stroke = style.stroke
                if (stroke != null && stroke.color != OverlayColor.Invisible) {
                    this[COLOR] = JsonPrimitive(stroke.color.toRgbaString())
                    this[OUTLINE_COLOR] = JsonPrimitive(stroke.color.darkened().toRgbaString())
                } else {
                    this[OPACITY] = JsonPrimitive(0f)
                }
                if (stroke?.dashed == true) this[DASHED] = JsonPrimitive(true)
            }
            val center = Feature(line, JsonObject(centerProperties))

            // The Android implementation intentionally leaves labels without an element key. A
            // symbol click therefore falls through to the underlying clickable center line.
            val label = style.label?.let {
                Feature(
                    Point(geometry.center.toPosition()),
                    JsonObject(mapOf(LABEL to JsonPrimitive(it))),
                )
            }

            listOfNotNull(left, right, center, label)
        }
    }
}

internal fun JsonObject.toOverlayElementKey(): ElementKey? {
    val id = get(ELEMENT_ID)?.jsonPrimitive?.longOrNull ?: return null
    val type = get(ELEMENT_TYPE)?.jsonPrimitive?.content ?: return null
    return ElementKey(ElementType.valueOf(type), id)
}

internal fun JsonObject.isOverlayElementDisabled(): Boolean =
    get(DISABLED)?.jsonPrimitive?.booleanOrNull ?: false

internal fun overlayLineWidth(tags: Map<String, String>): Float = when (tags["highway"]) {
    "motorway" -> 8f
    "motorway_link" -> 4f
    "trunk", "primary", "secondary", "tertiary" -> 6f
    "service", "track", "busway" -> 3f
    "path", "cycleway", "footway", "bridleway", "steps" -> 1f
    null -> 2f
    else -> 4f
}

internal fun isOverlayBridge(tags: Map<String, String>): Boolean =
    tags["bridge"] != null && tags["bridge"] != "no"

internal fun StyledElement.overlayIcon(): DrawableResource? = when (val value = style) {
    is OverlayStyle.Point -> value.icon
    is OverlayStyle.Polygon -> value.icon
    is OverlayStyle.Polyline -> null
}

private fun elementProperties(
    key: ElementKey,
    disabled: Boolean,
): MutableMap<String, JsonElement> = mutableMapOf<String, JsonElement>(
    ELEMENT_ID to JsonPrimitive(key.id),
    ELEMENT_TYPE to JsonPrimitive(key.type.name),
).apply {
    if (disabled) this[DISABLED] = JsonPrimitive(true)
}

private fun MutableMap<String, JsonElement>.applyPointStyle(
    icon: DrawableResource?,
    label: String?,
): MutableMap<String, JsonElement> = apply {
    icon?.let { this[ICON] = JsonPrimitive(plainStyleImageId(it)) }
    label?.let { this[LABEL] = JsonPrimitive(it) }
}

private fun MutableMap<String, JsonElement>.applyStroke(
    stroke: OverlayStyle.Stroke,
    width: Float,
    offset: Float,
): MutableMap<String, JsonElement> = apply {
    this[WIDTH] = JsonPrimitive(width)
    this[OFFSET] = JsonPrimitive(offset)
    if (stroke.color != OverlayColor.Invisible) {
        this[COLOR] = JsonPrimitive(stroke.color.toRgbaString())
    } else {
        this[OPACITY] = JsonPrimitive(0f)
    }
    if (stroke.dashed) this[DASHED] = JsonPrimitive(true)
}

private fun Color.darkened(): Color = Color(
    red = red * 0.67f,
    green = green * 0.67f,
    blue = blue * 0.67f,
    alpha = alpha,
)

internal fun Color.toRgbaString(): String {
    val argb = toArgb()
    return "rgba(${(argb shr 16) and 0xff}, ${(argb shr 8) and 0xff}, ${argb and 0xff}, $alpha)"
}

internal const val COLOR = "color"
internal const val OUTLINE_COLOR = "outline-color"
internal const val OPACITY = "opacity"
internal const val WIDTH = "width"
internal const val OFFSET = "offset"
internal const val DASHED = "dashed"
internal const val BRIDGE = "bridge"
internal const val HEIGHT = "height"
internal const val MIN_HEIGHT = "min-height"
internal const val ICON = "icon"
internal const val LABEL = "label"
private const val ELEMENT_TYPE = "element_type"
private const val ELEMENT_ID = "element_id"
// Keep the legacy wire name even though a true value means the element is disabled.
private const val DISABLED = "clickable"
