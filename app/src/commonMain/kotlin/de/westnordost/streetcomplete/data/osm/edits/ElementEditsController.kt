package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataUpdates

interface ElementEditsController : ElementEditsSource {

    /** Add new unsynced edit to the to-be-uploaded queue */
    fun add(
        type: ElementEditType,
        geometry: ElementGeometry,
        source: String,
        action: ElementEditAction,
        isNearUserLocation: Boolean
    )

    /** Delete old synced (aka uploaded) edits older than the given timestamp. Used to clear
     *  the undo history */
    fun deleteSyncedOlderThan(timestamp: Long): Int

    fun markSynced(edit: ElementEdit, elementUpdates: MapDataUpdates)

    fun markSyncFailed(edit: ElementEdit)

    /** Undo edit with the given id. If unsynced yet, will delete the edit if it is undoable. If
     *  already synced, will add a revert of that edit as a new edit, if possible */
    fun undo(edit: ElementEdit): Boolean
}
