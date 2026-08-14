package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.Listeners

class TeamModeQuestFilterControllerImpl(
    private val createdElementsSource: CreatedElementsSource,
    private val prefs: Preferences
) : TeamModeQuestFilterController {

    private val teamSize: Int get() = prefs.teamModeSize

    override val indexInTeam: Int get() = prefs.teamModeIndexInTeam

    override val isEnabled: Boolean get() = teamSize > 0

    private val listeners = Listeners<TeamModeQuestFilterSource.Listener>()

    override fun isVisible(quest: Quest): Boolean =
        !isEnabled
            || quest.stableId < 0
            || quest is OsmQuest && createdElementsSource.contains(quest.elementType, quest.elementId)
            || quest.stableId % teamSize == indexInTeam.toLong()

    private val Quest.stableId: Long get() = when (this) {
        is OsmQuest -> elementId
        is OsmNoteQuest -> id
        else -> 0
    }

    override fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        prefs.teamModeSize = teamSize
        prefs.teamModeIndexInTeam = indexInTeam
        listeners.forEach { it.onTeamModeChanged(true) }
    }

    override fun disableTeamMode() {
        prefs.teamModeSize = -1
        prefs.teamModeIndexInTeam = -1
        listeners.forEach { it.onTeamModeChanged(false) }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: TeamModeQuestFilterSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: TeamModeQuestFilterSource.Listener) {
        listeners.remove(listener)
    }
}
