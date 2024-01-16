package de.westnordost.streetcomplete.data.osmnotes.notequests

/** Controller for managing which osm note quests have been hidden by user interaction */
interface OsmNoteQuestsHiddenController : OsmNoteQuestsHiddenSource {

    /** Mark the note quest as hidden by user interaction */
    fun hide(questId: Long)

    /** Un-hides a specific hidden quest by user interaction */
    fun unhide(questId: Long): Boolean

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int
}
