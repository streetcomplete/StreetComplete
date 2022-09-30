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

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val filter = try {
            prefs.getString(Prefs.CUSTOM_OVERLAY_FILTER, "")?.toElementFilterExpression() ?: return emptySequence()
        } catch (e: ParseException) { return emptySequence() }
        return mapData
            .filter(filter)
            .map { it to getStyle(it) }
    }

    override fun createForm(element: Element?) = CustomOverlayForm()
}

private fun getStyle(element: Element): Style {
    val color = Color.LIME
    return when {
        element is Node -> PointStyle("ic_custom_overlay_poi", element.tags["name"])
        IS_AREA_EXPRESSION.matches(element) -> PolygonStyle(color, null)
        else -> PolylineStyle(StrokeStyle(color))
    }
}
