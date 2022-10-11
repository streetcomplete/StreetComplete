package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository

interface ElementEditAction {
    /** the number of new elements this edit creates. This needs to be stated in advance so that
     *  negative element ids can be reserved for this edit: The same element id needs to be used
     *  when applying the edit locally and when uploading the edit */
    val newElementsCount: NewElementsCount get() = NewElementsCount(0, 0, 0)

    /** Using the given map data repository (if necessary) and the id provider (if this action
     * creates new elements), this function should return all updated elements this action produces
     * when applied to the given element or throw a ElementConflictException
     * */
    fun createUpdates(
        mapDataRepository: MapDataRepository,
        idProvider: ElementIdProvider
    ): MapDataChanges
}

data class NewElementsCount(val nodes: Int, val ways: Int, val relations: Int)

interface IsActionRevertable {
    fun createReverted(idProvider: ElementIdProvider): ElementEditAction
}
interface IsRevertAction
