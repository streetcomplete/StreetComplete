package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestKey

/** Controller for managing which quests have been hidden by user interaction. */
interface QuestsHiddenController : QuestsHiddenSource, HideQuestController {
    /** Mark the quest as hidden by user interaction */
    override fun hide(key: QuestKey)

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: QuestKey): Boolean

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int
}
