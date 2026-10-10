package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.IsActionRevertable
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import kotlinx.serialization.Serializable

/** Action that updates the tags of several elements in one edit. Used when the answer to a quest
 *  on one element also implies tag changes on other, nearby elements.
 *
 *  If the tag changes cannot be applied to any one of the elements, the whole action conflicts. */
@Serializable
data class UpdateElementsTagsAction(
    val actions: List<UpdateElementTagsAction>
) : ElementEditAction, IsActionRevertable {

    init {
        require(actions.isNotEmpty())
    }

    override val elementKeys: List<ElementKey> get() = actions.flatMap { it.elementKeys }

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) =
        copy(actions = actions.map { it.idsUpdatesApplied(updatedIds) })

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ) = MapDataChanges(
        modifications = actions.flatMap { it.createUpdates(mapDataRepository, idProvider).modifications }
    )

    override fun createReverted(idProvider: ElementIdProvider) =
        RevertUpdateElementsTagsAction(actions.map { it.createReverted(idProvider) })
}

/** Contains the information necessary to apply a revert of tag changes made on several elements */
@Serializable
data class RevertUpdateElementsTagsAction(
    val actions: List<RevertUpdateElementTagsAction>
) : ElementEditAction, IsRevertAction {

    override val elementKeys: List<ElementKey> get() = actions.flatMap { it.elementKeys }

    override fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>) =
        copy(actions = actions.map { it.idsUpdatesApplied(updatedIds) })

    override fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ) = MapDataChanges(
        modifications = actions.flatMap { it.createUpdates(mapDataRepository, idProvider).modifications }
    )
}
