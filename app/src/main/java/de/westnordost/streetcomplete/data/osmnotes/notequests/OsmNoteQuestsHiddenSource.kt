package de.westnordost.streetcomplete.data.osmnotes.notequests

interface OsmNoteQuestsHiddenSource {

    interface Listener {
        fun onHid(edit: OsmNoteQuestHidden)
        fun onUnhid(edit: OsmNoteQuestHidden)
        fun onUnhidAll()
    }

    /** Get information about an osm note quest hidden by the user or null if it does not exist /
     *  has not been hidden */
    fun getHidden(questId: Long): OsmNoteQuestHidden?

    /** Get information about all osm note quests hidden by the user after the given [timestamp] */
    fun getAllHiddenNewerThan(timestamp: Long): List<OsmNoteQuestHidden>

    /** Get number of osm quests hidden by the user */
    fun countAll(): Long

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
