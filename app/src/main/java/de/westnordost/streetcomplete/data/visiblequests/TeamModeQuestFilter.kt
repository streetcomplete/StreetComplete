package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.quest.Quest
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
@Singleton class TeamModeQuestFilter @Inject internal constructor() {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  shared preferences */

    fun isVisible(quest: Quest): Boolean {
        // TODO: if team mode not enabled: return true

        // TODO: actual team mode numbers
        return quest.stableId % 10 == 0.toLong()
    }

    private val Quest.stableId: Long get() = when(this) {
        is OsmQuest -> elementId
        is OsmNoteQuest -> note.id
        else -> 0
    }
}
