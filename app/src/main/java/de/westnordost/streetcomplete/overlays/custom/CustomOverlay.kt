package de.westnordost.streetcomplete.overlays.custom

import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.ktx.isArea

class CustomOverlay(val prefs: ObservableSettings) : Overlay {

    override val title = R.string.custom_overlay_title
    override val icon = R.drawable.ic_custom_overlay
    override val changesetComment = "Edit user-defined element selection"
    override val wikiLink: String = "Tags"
    override val isCreateNodeEnabled get() = prefs.getString(Prefs.CUSTOM_OVERLAY_IDX_FILTER, "").startsWith("nodes")

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val filter = try {
            prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, prefs), "").toElementFilterExpression()
        } catch (e: ParseException) { return emptySequence() }
        val colorKeyPref = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, prefs), "").let {
            if (it.startsWith("!")) it.substringAfter("!")
            else it
        }
        val colorKeySelector = try { colorKeyPref.takeIf { it.isNotBlank() }?.toRegex() }
            catch (_: Exception) { null }
        val dashFilter = try {
            val string = prefs.getString(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_DASH_FILTER, prefs), "").takeIf { it.isNotBlank() }
            string?.let { "ways with $it".toElementFilterExpression() }
        } catch (_: Exception) { null }
        val missingColor = if (prefs.getBoolean(getCurrentCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_HIGHLIGHT_MISSING_DATA, prefs), true))
                Color.DATA_REQUESTED
            else
                Color.INVISIBLE
        return mapData
            .filter(filter)
            .map { it to getStyle(it, colorKeySelector, dashFilter, missingColor) }
    }

    override fun createForm(element: Element?) = CustomOverlayForm()
}

private fun getStyle(element: Element, colorKeySelector: Regex?, dashFilter: ElementFilterExpression?, defaultMissingColor: String): Style {
    val color by lazy {
        if (colorKeySelector == null) Color.LIME
        else {
            val colorString = element.tags.mapNotNull {
                // derive color from all matching tags
                if (it.key.matches(colorKeySelector)) it.value + it.key
                else null
            }.sorted().joinToString() // sort because tags hashMap doesn't have a defined order
            if (colorString.isEmpty()) defaultMissingColor
            else createColorFromString(colorString)
        }
    }

    var leftColor = ""
    var rightColor = ""
    var centerColor: String? = null
    // get left/right style if there is some match
    if (colorKeySelector != null && element !is Node && !element.isArea()) { // avoid doing needless work here
        val leftColorTags = mutableListOf<String>()
        val rightColorTags = mutableListOf<String>()
        val centerColorTags = mutableListOf<String>()
        for ((k, v) in element.tags) {
            if (!k.matches(colorKeySelector)) continue
            // create color in a way that left, right and both match in color -> strip side from tags
            if (v == "both" || k.contains(":both")) {
                val t = v + k.replace(":both", "")
                leftColorTags.add(t)
                rightColorTags.add(t)
                continue
            }
            if (v == "right" || k.contains(":right")) {
                rightColorTags.add(v + k.replace(":right", ""))
                continue
            }
            if (v == "left" || k.contains(":left")) {
                leftColorTags.add(v + k.replace(":left", ""))
                continue
            }
            // only use a center color if there is a match that is not related to left/right/both
            centerColorTags.add(v + k)
        }
        // make sure to use all matching color tags
        if (leftColorTags.isNotEmpty())
            leftColor = createColorFromString(leftColorTags.sorted().joinToString())
        if (rightColorTags.isNotEmpty())
            rightColor = createColorFromString(rightColorTags.sorted().joinToString())
        if (centerColorTags.isNotEmpty())
            centerColor = createColorFromString(centerColorTags.sorted().joinToString())
    }


    return when {
//        element is Node -> PointStyle(R.drawable.ic_custom_overlay_node, getNameLabel(element.tags), color)
        // MapLibre can only use colors with sdf icons, not with normal images
        element is Node -> PointStyle(R.drawable.ic_preset_maki_circle, getNameLabel(element.tags), color)
        element.isArea() -> PolygonStyle(color, label = getNameLabel(element.tags))
        // no labels for lines, because this often leads to duplicate labels e.g. for roads
        leftColor.isNotEmpty() || rightColor.isNotEmpty() -> PolylineStyle(
            stroke = centerColor?.let { StrokeStyle(it, dashFilter?.matches(element) == true) },
            strokeLeft = leftColor.takeIf { it.isNotEmpty() }?.let { StrokeStyle(it) },
            strokeRight = rightColor.takeIf { it.isNotEmpty() }?.let { StrokeStyle(it) }
        )
        else -> PolylineStyle(StrokeStyle(color, dashFilter?.matches(element) == true))
    }
}

private fun createColorFromString(string: String): String {
    val c = string.hashCode().toString(16)
    return when {
        c.length >= 6 -> "#${c.subSequence(c.length - 6, c.length)}"
        else -> createColorFromString("${c}1") // the 1 is there to avoid very similar colors for numbers
    }
}

fun getIndexedCustomOverlayPref(pref: String, index: Int) = pref.replace("idx", index.toString())
fun getCurrentCustomOverlayPref(pref: String, prefs: ObservableSettings) = getIndexedCustomOverlayPref(pref, prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0))
fun getCustomOverlayIndices(prefs: SharedPreferences) = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")!!
    .split(",").mapNotNull { it.toIntOrNull() }
fun getCustomOverlayIndices(prefs: Preferences) = prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0")
    .split(",").mapNotNull { it.toIntOrNull() }
