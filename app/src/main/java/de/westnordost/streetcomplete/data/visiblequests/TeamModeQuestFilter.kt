package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.quest.Quest
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
@Singleton class TeamModeQuestFilter @Inject internal constructor(
    private val prefs: SharedPreferences
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  shared preferences */

    private val teamSize: Int get() = prefs.getInt(Prefs.TEAM_MODE_TEAM_SIZE, -1)
    val indexInTeam: Int get() = prefs.getInt(Prefs.TEAM_MODE_INDEX_IN_TEAM, -1)

    val isEnabled: Boolean get() = teamSize > 0

    fun isVisible(quest: Quest): Boolean =
        !isEnabled || quest.stableId % teamSize == indexInTeam.toLong()

    private val Quest.stableId: Long get() = when(this) {
        is OsmQuest -> elementId
        is OsmNoteQuest -> note.id
        else -> 0
    }

    fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        prefs.edit {
            putInt(Prefs.TEAM_MODE_TEAM_SIZE, teamSize)
            putInt(Prefs.TEAM_MODE_INDEX_IN_TEAM, indexInTeam)
        }
    }

    fun disableTeamMode() {
        prefs.edit().putInt(Prefs.TEAM_MODE_TEAM_SIZE, -1).apply()
    }
}
