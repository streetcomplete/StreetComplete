package de.westnordost.streetcomplete.overlays.custom

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.util.ktx.isArea

class CustomOverlay(val prefs: SharedPreferences) : Overlay {

    override val title = R.string.custom_overlay_title
    override val icon = R.drawable.ic_custom_overlay_poi
    override val changesetComment = "Edit user-defined element selection"
    override val wikiLink: String = "Tags"
    override val isCreateNodeEnabled get() = prefs.getString(Prefs.CUSTOM_OVERLAY_IDX_FILTER, "")!!.startsWith("nodes")

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val filter = try {
            prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, prefs), "")?.toElementFilterExpression() ?: return emptySequence()
        } catch (e: ParseException) { return emptySequence() }
        val colorKeySelector = try {
            prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "")?.takeIf { it.isNotEmpty() }?.toRegex()
        } catch (_: Exception) { null }
        return mapData
            .filter(filter)
            .map { it to getStyle(it, colorKeySelector) }
    }

    override fun createForm(element: Element?) = CustomOverlayForm()
}

private fun getStyle(element: Element, colorKeySelector: Regex?): Style {
    val color by lazy {
        if (colorKeySelector == null) Color.LIME
        else createColorFromString(element.tags.mapNotNull {
                if (it.key.matches(colorKeySelector))
                    it.value + it.key
                else null
            }.joinToString().takeIf { it.isNotEmpty() })
    }

    // get left/right style if there is some match
    var leftColor = ""
    var rightColor = ""
    var centerColor: String? = null
    if (colorKeySelector != null && element !is Node && !element.isArea()) // avoid doing needless work
        for ((k, v) in element.tags) {
            if (!k.matches(colorKeySelector)) continue
            // contains or endsWith? contains will also match things like sidewalk:left:surface, which may be wanted or not...
            if (v == "both" || k.contains(":both")) {
                // create color in a way that left, right and both match in color
                leftColor = createColorFromString(v + k.replace(":both", ""))
                rightColor = leftColor
                continue
            }
            if (v == "right" || k.contains(":right")) {
                rightColor = createColorFromString(v + k.replace(":right", ""))
                continue
            }
            if (v == "left" || k.contains(":left")) {
                leftColor = createColorFromString(v + k.replace(":left", ""))
                continue
            }
            // only use a center color if there is a match that is not related to left/right/both
            centerColor = createColorFromString(v + k)
        }

    return when {
        element is Node -> PointStyle("ic_custom_overlay_poi", element.tags["name"]) // currently no coloring possible...
        element.isArea() -> PolygonStyle(color, label = element.tags["name"])
        leftColor.isNotEmpty() || rightColor.isNotEmpty() -> PolylineStyle(
            stroke = centerColor?.let { StrokeStyle(it) },
            strokeLeft = leftColor.takeIf { it.isNotEmpty() }?.let { StrokeStyle(it) },
            strokeRight = rightColor.takeIf { it.isNotEmpty() }?.let { StrokeStyle(it) }
        )
        else -> PolylineStyle(StrokeStyle(color)) // no label for lines, because this often leads to duplicate labels e.g. for roads
    }
}

private fun createColorFromString(string: String?): String {
    if (string == null) return Color.DATA_REQUESTED
    val c = string.hashCode().toString(16)
    return when {
        c.length >= 6 -> "#${c.subSequence(c.length - 6, c.length)}"
        else -> createColorFromString("${c}1") // the 1 is there to avoid very similar colors for numbers
    }
}

fun getIndexedCustomOverlayPref(pref: String, index: Int) = pref.replace("idx", index.toString())
fun getCurrentCustomOverlayPref(pref: String, prefs: SharedPreferences) = getIndexedCustomOverlayPref(pref, prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))
