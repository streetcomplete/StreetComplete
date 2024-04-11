package de.westnordost.streetcomplete.data.visiblequests

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.Listeners

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
class TeamModeQuestFilter internal constructor(
    private val createdElementsSource: CreatedElementsSource,
    private val prefs: ObservableSettings
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  shared preferences */

    private val teamSize: Int get() = prefs.getInt(Prefs.TEAM_MODE_TEAM_SIZE, -1)
    val indexInTeam: Int get() = prefs.getInt(Prefs.TEAM_MODE_INDEX_IN_TEAM, -1)

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
        prefs.putInt(Prefs.TEAM_MODE_TEAM_SIZE, teamSize)
        prefs.putInt(Prefs.TEAM_MODE_INDEX_IN_TEAM, indexInTeam)
        listeners.forEach { it.onTeamModeChanged(true) }
    }

    fun disableTeamMode() {
        prefs.remove(Prefs.TEAM_MODE_TEAM_SIZE)
        prefs.remove(Prefs.TEAM_MODE_INDEX_IN_TEAM)
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
