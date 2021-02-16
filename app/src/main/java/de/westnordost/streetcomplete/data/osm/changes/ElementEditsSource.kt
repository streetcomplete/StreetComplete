package de.westnordost.streetcomplete.data.osm.changes

interface ElementEditsSource {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        fun onAddedEdit(edit: ElementEdit) {}
        fun onSyncedEdit(edit: ElementEdit) {}
        fun onDeletedEdit(edit: ElementEdit) {}
    }

    /** Count of unsynced edits that count towards the statistics. That is, unsynced note stuff
     *  doesn't count and reverts of edits count negative */
    fun getEditsCountSolved(): Int

    /** Count of unsynced a.k.a to-be-uploaded edits */
    fun getUnsyncedEditsCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
