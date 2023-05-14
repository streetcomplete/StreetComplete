package de.westnordost.streetcomplete.overlays.restriction

import android.graphics.Color.parseColor
import androidx.core.graphics.ColorUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.util.ktx.toARGBString

class RestrictionOverlay : Overlay {
    // show restriction icons? will need to add property for rotation / angle
    // but according to tangram docs, angle is a number or string, this would need a function...
    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val restrictions = mapData.relations.filter { it.tags["type"] == "restriction" }
        val restrictionsByWayMemberId = HashMap<Long, MutableList<Relation>>(restrictions.size)
        restrictions.forEach { restriction ->
            for (member in restriction.members) {
                if (member.type != ElementType.WAY) continue
                val list = restrictionsByWayMemberId.getOrPut(member.ref) { ArrayList(2) }
                list.add(restriction)
            }
        }
        // don't highlight via nodes... or do they matter?
        //  actually the do matter in some cases, e.g. no-u-turn with from and to being the same way
        //  ideally the via nodes would have the correct icon, and then of course need rotation too
        return mapData.filter("ways with highway ~ ${ALL_ROADS.joinToString("|")}").map { it to getStyle(it as Way, restrictionsByWayMemberId) }
    }

    override fun createForm(element: Element?): AbstractOverlayForm = RestrictionOverlayForm()

    override val changesetComment: String = "Specify turn restrictions"
    override val icon: Int = R.drawable.ic_overlay_restriction
    override val title: Int = R.string.restriction_overlay_title
    override val wikiLink: String = "Relation:restriction"

    // may return the same way multiple times if it has more than one restriction
    // though maybe only once with some sort of mixed color would be better?
    private fun getStyle(way: Way, restrictionsByWayMemberId: Map<Long, List<Relation>>): Style {
        // no highlight if road has no restrictions
        val relations = restrictionsByWayMemberId[way.id] ?: return PolylineStyle(StrokeStyle(Color.INVISIBLE))

        // merge colors if we have 2 relations on one way
        val color = if (relations.size == 2) {
            val colors = relations.map { it.getColor(way.id) }
            if (colors.first() == colors.last())
                colors.first()
            else
                toARGBString(ColorUtils.blendARGB(parseColor(colors.first()), parseColor(colors.last()), 0.5f))
        } else
            relations.first().getColor(way.id)
        return PolylineStyle(StrokeStyle(color))
    }
}

private fun Relation.getColor(wayId: Long): String {
    if (!isSupportedRestrictionRelation()) return Color.BLACK
    val role = members.firstOrNull { it.type == ElementType.WAY && it.ref == wayId }?.role ?: return Color.INVISIBLE
    return getColor(role, getRestrictionType()!!).replace("#", "#90") // make it transparent for at least some support of multiple relations on a single way
}

private fun getColor(role: String, restriction: String): String = when {
    restriction.startsWith("no_") && role == "from" -> Color.GOLD
    restriction.startsWith("no_") && role == "to" -> Color.ORANGE
    restriction.startsWith("only_") && role == "from" -> Color.AQUAMARINE
    restriction.startsWith("only_") && role == "to" -> Color.BLUE
    role == "via" -> Color.LIME
    else -> Color.BLACK
}

// support restrictions with 1 from way, 1 to way, 1 via node or 1+ via ways
// and additionally, ways need to be connected (but that is more complicated, and not checked)
fun Relation.isSupportedRestrictionRelation(): Boolean {
    if (tags["type"] != "restriction") return false
    if (getRestrictionType() !in restrictionTypes) return false
    if (members.count { it.type == ElementType.WAY && it.role == "from" } != 1) return false
    if (members.count { it.type == ElementType.WAY && it.role == "to" } != 1) return false
    val viaWayCount = members.count { it.type == ElementType.WAY && it.role == "via" }
    val viaNodeCount = members.count { it.type == ElementType.NODE && it.role == "via" }
    if (viaNodeCount > 1) return false
    if (viaNodeCount != 0 && viaWayCount != 0) return false
    return true
}

fun Relation.getRestrictionType() = tags["restriction"] ?: tags["restriction:conditional"]?.substringBefore("@")?.trim()
    ?: tags["restriction:hgv"] ?: tags["restriction:bus"]
