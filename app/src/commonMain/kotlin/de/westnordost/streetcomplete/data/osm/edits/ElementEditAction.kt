package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository

interface ElementEditAction {
    /** the number of new elements this edit creates. This needs to be stated in advance so that
     *  negative element ids can be reserved for this edit: The same element id needs to be used
     *  when applying the edit locally and when uploading the edit */
    val newElementsCount: NewElementsCount get() = NewElementsCount(0, 0, 0)

    /** the keys to elements this action is based on. It can be none (when creating a node) or it
     *  can be several (e.g. when reverting splitting a way; inserting a node into several ways,
     *  ...).
     *  When undoing (=deleting) an edit that creates elements onto which other edits are based on,
     *  these edits are also deleted (recursively). */
    val elementKeys: List<ElementKey>

    /** Using the given map data repository (if necessary) and the id provider (if this action
     *  creates new elements), this function should return all updated elements this action produces
     *  when applied to the given element.
     *  @throws ConflictException when a conflict occured when trying to apply the changes
     *  @throws IllegalArgumentException if any of the created changes contain invalid data (e.g. if
     *          key or value of any OSM tag is too long)
     */
    fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges

    /** return a copy of this action where the temporary ids of the elements referred to have been
     *  replaced with the updated ones */
    fun idsUpdatesApplied(updatedIds: Map<ElementKey, Long>): ElementEditAction
}

data class NewElementsCount(val nodes: Int, val ways: Int, val relations: Int)

interface IsActionRevertable {
    fun createReverted(idProvider: ElementIdProvider): ElementEditAction
}
interface IsRevertAction
