package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.quest.QuestKey

interface ElementEditsSource {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        fun onAddedEdit(edit: ElementEdit)
        fun onAddedEdit(edit: ElementEdit, key: QuestKey?) = onAddedEdit(edit) // need to do it this weird way, otherwise must change everything that implement listener
        fun onSyncedEdit(edit: ElementEdit)
        fun onSyncedEdit(edit: ElementEdit, updatedEditIds: Boolean) = onSyncedEdit(edit) // see above
        // may be several because deleting one element edit leads to the deletion of all edits that
        // are based on that edit. E.g. splitting a way, then editing the newly created way segments
        fun onDeletedEdits(edits: List<ElementEdit>)
    }

    /** Get an edit by its id */
    fun get(id: Long): ElementEdit?

    /** Get all edits (synced and unsynced) */
    fun getAll(): List<ElementEdit>

    /** Get all unsynced edits */
    fun getAllUnsynced(): List<ElementEdit>

    /** Count of unsynced edits that count towards the statistics. That is, reverts of edits
     *  count negative */
    fun getPositiveUnsyncedCount(): Int

    /** Count of unsynced a.k.a to-be-uploaded edits */
    fun getUnsyncedCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
