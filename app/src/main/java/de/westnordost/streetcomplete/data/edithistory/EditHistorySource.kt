package de.westnordost.streetcomplete.data.edithistory

interface EditHistorySource {
    interface Listener {
        fun onAdded(added: Edit)
        fun onSynced(synced: Edit)
        fun onDeleted(deleted: List<Edit>)
        fun onInvalidated()
    }

    fun get(key: EditKey): Edit?

    fun getAll(): List<Edit>
    fun getCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
