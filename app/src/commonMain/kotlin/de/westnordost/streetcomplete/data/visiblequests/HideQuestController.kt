package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestKey

interface HideQuestController {
    /** Mark the quest as hidden by user interaction */
    fun hide(key: QuestKey)
}
