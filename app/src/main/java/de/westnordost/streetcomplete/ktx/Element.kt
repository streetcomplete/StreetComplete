package de.westnordost.streetcomplete.ktx

import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.meta.IS_AREA_EXPRESSION
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.Level
import de.westnordost.streetcomplete.osm.LevelRange
import de.westnordost.streetcomplete.osm.SingleLevel
import de.westnordost.streetcomplete.osm.toLevelsOrNull

fun Element.copy(
    id: Long = this.id,
    tags: Map<String, String> = this.tags,
    version: Int = this.version,
    timestampEdited: Long = this.timestampEdited,
): Element {
    return when (this) {
        is Node -> Node(id, position, tags, version, timestampEdited)
        is Way -> Way(id, ArrayList(nodeIds), tags, version, timestampEdited)
        is Relation -> Relation(id, ArrayList(members), tags, version, timestampEdited)
    }
}

val Element.geometryType: GeometryType get() =
    when {
        type == ElementType.NODE -> GeometryType.POINT
        isArea() -> GeometryType.AREA
        type == ElementType.RELATION -> GeometryType.RELATION
        else -> GeometryType.LINE
    }

fun Element.isArea(): Boolean {
    return when (this) {
        is Way -> isClosed && IS_AREA_EXPRESSION.matches(this)
        is Relation -> tags["type"] == "multipolygon"
        else -> false
    }
}

/** get for which level(s) the element is defined, if any.
 *  repeat_on is interpreted the same way as level */
fun Element.getLevelsOrNull(): List<Level>? {
    val levels = tags["level"]?.toLevelsOrNull()
    val repeatOns = tags["repeat_on"]?.toLevelsOrNull()
    return if (levels == null) {
        if (repeatOns == null) null else repeatOns
    } else {
        if (repeatOns == null) levels else levels + repeatOns
    }
}

/** Return all levels of these elements, sorted ascending */
fun Iterable<Element>.getSelectableLevels(): List<Double> {
    val allLevels = mutableSetOf<Double>()
    for (e in this) {
        val levels = e.getLevelsOrNull() ?: continue
        for (level in levels) {
            when (level) {
                is LevelRange -> allLevels.addAll(level.getSelectableLevels())
                is SingleLevel -> allLevels.add(level.level)
            }
        }
    }
    return allLevels.sorted()
}
