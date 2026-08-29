package de.westnordost.streetcomplete.data.visiblequests

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
interface TeamModeQuestFilterController : TeamModeQuestFilterSource {

    fun enableTeamMode(teamSize: Int, indexInTeam: Int)

    fun disableTeamMode()
}
