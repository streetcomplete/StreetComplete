package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsSource
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.Listeners

/** Controller for filtering all quests that are hidden because they are shown to other users in
 *  team mode. Takes care of persisting team mode settings and notifying listeners about changes */
interface TeamModeQuestFilterController : TeamModeQuestFilterSource {

    fun enableTeamMode(teamSize: Int, indexInTeam: Int)

    fun disableTeamMode()
}
