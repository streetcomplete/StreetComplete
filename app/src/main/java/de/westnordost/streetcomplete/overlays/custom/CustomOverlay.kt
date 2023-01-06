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
import de.westnordost.streetcomplete.osm.IS_AREA_EXPRESSION
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.StrokeStyle

class CustomOverlay(val prefs: SharedPreferences) : Overlay {

    override val title = R.string.custom_overlay_title
    override val icon = R.drawable.ic_custom_overlay_poi
    override val changesetComment = "Edit user-defined element selection"
    override val wikiLink: String = "Tags"
    override val isCreateNodeEnabled get() = prefs.getString(Prefs.CUSTOM_OVERLAY_FILTER, "")!!.startsWith("nodes")

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val filter = try {
            prefs.getString(Prefs.CUSTOM_OVERLAY_FILTER, "")?.toElementFilterExpression() ?: return emptySequence()
        } catch (e: ParseException) { return emptySequence() }
        val colorKeySelector = try {
            prefs.getString(Prefs.CUSTOM_OVERLAY_COLOR_KEY, "")?.takeIf { it.isNotEmpty() }?.toRegex()
        } catch (_: Exception) { null }
        return mapData
            .filter(filter)
            .map { it to getStyle(it, colorKeySelector) }
    }

    override fun createForm(element: Element?) = CustomOverlayForm()
}

private fun getStyle(element: Element, colorKeySelector: Regex?): Style {
    val color = if (colorKeySelector == null) Color.LIME
        else createColorFromString(element.tags.mapNotNull {
                if (it.key.matches(colorKeySelector))
                    it.value + it.key
                else null
            }.joinToString().takeIf { it.isNotEmpty() })
    return when {
        element is Node -> PointStyle("ic_custom_overlay_poi", element.tags["name"])
        IS_AREA_EXPRESSION.matches(element) -> PolygonStyle(color, label = element.tags["name"])
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
