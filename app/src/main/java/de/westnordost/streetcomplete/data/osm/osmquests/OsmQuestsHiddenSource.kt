package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestsHiddenSource {

    interface Listener {
        fun onHid(edit: OsmQuestHidden)
        fun onUnhid(edit: OsmQuestHidden)
        fun onUnhidAll()
    }

    /** Returns whether the given osm quest is hidden by the user */
    fun isHidden(key: OsmQuestKey): Boolean

    /** Get information about an osm quest hidden by the user or null if it does not exist / has not
     *  been hidden */
    fun get(key: OsmQuestKey): OsmQuestHidden?

    /** Get information about all osm quests hidden by the user after the given [timestamp] */
    fun getAllNewerThan(timestamp: Long): List<OsmQuestHidden>

    /** Get number of osm quests hidden by the user */
    fun countAll(): Long

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
