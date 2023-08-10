package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay

fun createNodeAction(
    positionOnWay: PositionOnWay,
    mapDataWithEditsSource: MapDataWithEditsSource,
    createChanges: (StringMapChangesBuilder) -> Unit
): ElementEditAction? {
    when (positionOnWay) {
        is PositionOnWaySegment -> {
            val tagChanges = StringMapChangesBuilder(mapOf())
            createChanges(tagChanges)
            val insertIntoWayAt = InsertIntoWayAt(
                positionOnWay.wayId,
                positionOnWay.segment.first,
                positionOnWay.segment.second
            )
            return CreateNodeAction(positionOnWay.position, tagChanges, listOf(insertIntoWayAt))
        }
        is VertexOfWay -> {
            val node = mapDataWithEditsSource.getNode(positionOnWay.nodeId) ?: return null
            val tagChanges = StringMapChangesBuilder(node.tags)
            createChanges(tagChanges)
            val containingWayIds = mapDataWithEditsSource.getWaysForNode(positionOnWay.nodeId).map { it.id }
            return CreateNodeFromVertexAction(node, tagChanges.create(), containingWayIds)
        }
    }
}
