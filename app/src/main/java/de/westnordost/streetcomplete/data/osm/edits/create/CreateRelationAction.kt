package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.NewElementsCount
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.RelationMember
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.Serializable

@Serializable
data class CreateRelationAction (
    val tags: Map<String, String>,
    val members: List<RelationMember>
) : ElementEditAction/*, IsActionRevertable*/ {

    override val newElementsCount get() = NewElementsCount(0, 0, 1)

    override val elementKeys: List<ElementKey> get() = members.map { it.key }

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) = copy(
        members = members.map { it.copy(ref = updatedIds[it.key] ?: it.ref) }
    )

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges {
        val newRelation = createRelation(idProvider)

        return MapDataChanges(creations = listOf(newRelation))
    }

    // todo: make revertable, no reason not to
//    override fun createReverted(idProvider: ElementIdProvider) =
//        RevertCreateNodeAction(createNode(idProvider), insertIntoWays.map { it.wayId })

    private fun createRelation(idProvider: ElementIdProvider) =
        Relation(idProvider.nextRelationId(), members, tags, 1, nowAsEpochMilliseconds())
}
