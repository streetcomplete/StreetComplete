package de.westnordost.streetcomplete.data.osm.edits

interface ElementEditsSource {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        fun onAddedEdit(edit: ElementEdit)
        fun onSyncedEdit(edit: ElementEdit)
        // may be several because deleting one element edit leads to the deletion of all edits that
        // are based on that edit. E.g. splitting a way, then editing the newly created way segments
        fun onDeletedEdits(edits: List<ElementEdit>)
    }

    /** Count of unsynced edits that count towards the statistics. That is, reverts of edits
     *  count negative */
    fun getPositiveUnsyncedCount(): Int

    /** Count of unsynced a.k.a to-be-uploaded edits */
    fun getUnsyncedCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
