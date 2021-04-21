package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType

interface VisibleQuestTypeSource {

    /** interface to be notified of changed in quest type visibilities */
    interface Listener {
        fun onQuestTypeVisibilitiesChanged()
    }

    /** return whether the given quest type is visible */
    fun isVisible(questType: QuestType<*>): Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
