package de.westnordost.streetcomplete.data.edithistory

interface EditHistorySource {
    interface Listener {
        fun onAdded(edit: Edit)
        fun onSynced(edit: Edit)
        fun onDeleted(edits: List<Edit>)
        fun onInvalidated()
    }

    fun get(key: EditKey): Edit?

    fun getMostRecentUndoable(): Edit?

    fun getAll(allHidden: Boolean = false): List<Edit>
    fun getCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
