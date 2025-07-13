package de.westnordost.streetcomplete.quests.ferry

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.Relation

/** Return all ids of ways that are part of any ferry route relation */
fun wayIdsInFerryRoutes(relations: Collection<Relation>): Set<Long> {
    val result = HashSet<Long>()
    for (relation in relations) {
        if (relation.tags["route"] != "ferry") continue
        for (member in relation.members) {
            if (member.type != ElementType.WAY) continue
            result.add(member.ref)
        }
    }
    return result
}
