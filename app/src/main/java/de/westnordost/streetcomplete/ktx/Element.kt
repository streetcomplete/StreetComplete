package de.westnordost.streetcomplete.ktx

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import java.util.ArrayList

fun Element.copy(newId: Long = id, newVersion: Int = version): Element {
    val tags = tags?.let { HashMap(it) }
    return when (this) {
        is Node -> OsmNode(newId, newVersion, position, tags)
        is Way -> OsmWay(newId, newVersion, ArrayList(nodeIds), tags)
        is Relation -> OsmRelation(newId, newVersion, ArrayList(members), tags)
        else -> throw RuntimeException()
    }
}

fun Way.isClosed() = nodeIds.size >= 3 && nodeIds.first() == nodeIds.last()

fun Element.isArea(): Boolean {
    return when(this) {
        is Way -> isClosed() && IS_AREA_EXPR.matches(this)
        is Relation -> tags?.get("type") == "multipolygon"
        else -> false
    }
}

private val IS_AREA_EXPR = FiltersParser().parse("""
    ways with area = yes or area != no and (
    aeroway
    or amenity
    or boundary
    or building
    or craft
    or emergency
    or historic
    or landuse
    or leisure
    or office
    or place
    or public_transport
    or shop
    or tourism
    or building:part
    or aerialway = station
    or railway = station
    or (military and military != trench)
    or power ~ compensator|converter|generator|plant|substation
    or waterway ~ boatyard|dam|dock|riverbank|fuel
    or cemetery ~ sector|grave
    or natural ~ wood|scrub|heath|moor|grassland|fell|bare_rock|scree|shingle|sand|mud|water|wetland|glacier|beach|rock|sinkhole
    or man_made ~ beacon|bridge|campanile|dolphin|lighthouse|obelisk|observatory|tower|bunker_silo|chimney|gasometer|kiln|mineshaft|petroleum_well|silo|storage_tank|watermill|windmill|works|communications_tower|monitoring_station|street_cabinet|pumping_station|reservoir_covered|wastewater_plant|water_tank|water_tower|water_well|water_works
    )""")
