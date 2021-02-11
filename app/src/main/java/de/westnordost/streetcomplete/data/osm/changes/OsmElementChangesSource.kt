package de.westnordost.streetcomplete.data.osm.changes

interface OsmElementChangesSource {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        fun onAddedChange(change: OsmElementChange)
        fun onDeletedChange(change: OsmElementChange)
    }

    /** Count of unsynced changes that count towards the statistics. That is, unsynced note stuff
     *  doesn't count and reverts of changes count negative */
    fun getSolvedCount(): Int

    /** Count of unsynced a.k.a to-be-uploaded changes */
    fun getUnsyncedCount(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
