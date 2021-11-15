package de.westnordost.streetcomplete.ktx

import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.osm.mapdata.*
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
    return when(this) {
        is Way -> isClosed && IS_AREA_EXPR.matches(this)
        is Relation -> tags["type"] == "multipolygon"
        else -> false
    }
}

private val IS_AREA_EXPR = """
    ways with area = yes or area != no and (
    aeroway
    or amenity
    or boundary
    or building
    or craft
    or (emergency and emergency !~ yes|no)
    or historic
    or landuse
    or (leisure and leisure != track)
    or office
    or place
    or public_transport
    or shop
    or tourism
    or building:part
    or aerialway = station
    or railway ~ platform|station
    or (military and military != trench)
    or power ~ compensator|converter|generator|plant|substation
    or waterway ~ boatyard|dam|dock|riverbank|fuel
    or cemetery ~ sector|grave
    or natural ~ wood|scrub|heath|moor|grassland|fell|bare_rock|scree|shingle|sand|mud|water|wetland|glacier|beach|rock|sinkhole
    or man_made ~ beacon|bridge|campanile|dolphin|lighthouse|obelisk|observatory|tower|bunker_silo|chimney|gasometer|kiln|mineshaft|petroleum_well|silo|storage_tank|watermill|windmill|works|communications_tower|monitoring_station|street_cabinet|pumping_station|reservoir_covered|wastewater_plant|water_tank|water_tower|water_well|water_works
    )""".toElementFilterExpression()

fun Element.isSomeKindOfShop(): Boolean = IS_SOME_KIND_OF_SHOP_EXPR.matches(this)

private val IS_SOME_KIND_OF_SHOP_EXPR =
    ("nodes, ways, relations with " + isKindOfShopExpression()).toElementFilterExpression()

/** get for which level(s) the element is defined, if any.
 *  repeat_on is interpreted the same way as level */
fun Element.getLevelsOrNull(): List<Level>? {
    val levels = tags["level"]?.toLevelsOrNull()
    val repeatOns =  tags["repeat_on"]?.toLevelsOrNull()
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
            when(level) {
                is LevelRange -> allLevels.addAll(level.getSelectableLevels())
                is SingleLevel -> allLevels.add(level.level)
            }
        }
    }
    return allLevels.sorted()
}
