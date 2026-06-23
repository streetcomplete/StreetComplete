package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.Quest

interface TeamModeQuestFilterSource {
    interface Listener {
        fun onTeamModeChanged(enabled: Boolean)
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    val indexInTeam: Int

    val isEnabled: Boolean

    fun isVisible(quest: Quest): Boolean
}
