package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.Listeners

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
class TeamModeQuestFilter internal constructor(
    private val createdElementsSource: CreatedElementsSource,
    private val prefs: Preferences
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  shared preferences */

    private val teamSize: Int get() = prefs.teamModeSize
    val indexInTeam: Int get() = prefs.teamModeIndexInTeam

    val isEnabled: Boolean get() = teamSize > 0

    interface TeamModeChangeListener {
        fun onTeamModeChanged(enabled: Boolean)
    }
    private val listeners = Listeners<TeamModeChangeListener>()

    fun isVisible(quest: Quest): Boolean =
        !isEnabled
        || quest.stableId < 0
        || quest is OsmQuest && createdElementsSource.contains(quest.elementType, quest.elementId)
        || quest.stableId % teamSize == indexInTeam.toLong()

    private val Quest.stableId: Long get() = when (this) {
        is OsmQuest -> elementId
        is OsmNoteQuest -> id
        else -> 0
    }

    fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        prefs.teamModeSize = teamSize
        prefs.teamModeIndexInTeam = indexInTeam
        listeners.forEach { it.onTeamModeChanged(true) }
    }

    fun disableTeamMode() {
        prefs.teamModeSize = -1
        prefs.teamModeIndexInTeam = -1
        listeners.forEach { it.onTeamModeChanged(false) }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    fun addListener(listener: TeamModeChangeListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: TeamModeChangeListener) {
        listeners.remove(listener)
    }
}
