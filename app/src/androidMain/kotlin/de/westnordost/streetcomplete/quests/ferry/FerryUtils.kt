package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry

fun isApplicableTo(element: Element, mapData: MapDataWithGeometry): Boolean {
    val tags = element.tags

    // Filter out ferries that are part of a ferry route relation
    if (tags["route"] == "ferry") {
        val isPartOfFerryRelation = mapData.relations.any { relation ->
            relation.tags["route"] == "ferry" &&
                relation.members.any { member ->
                    member.type == ElementType.WAY && member.ref == element.id
                }
        }
        if (isPartOfFerryRelation) {
            return false
        }
    }

    return true
}
