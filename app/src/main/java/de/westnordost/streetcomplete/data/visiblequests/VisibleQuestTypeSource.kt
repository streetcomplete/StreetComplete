package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType

interface VisibleQuestTypeSource {

    /** interface to be notified of changed in quest type visibilities */
    interface Listener {
        fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean)
        /** Called when a number of quest type visibilities changed */
        fun onQuestTypeVisibilitiesChanged()
    }

    /** return whether the given quest type is visible */
    fun isVisible(questType: QuestType): Boolean

    fun getVisible(presetId: Long? = null): Set<QuestType>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
