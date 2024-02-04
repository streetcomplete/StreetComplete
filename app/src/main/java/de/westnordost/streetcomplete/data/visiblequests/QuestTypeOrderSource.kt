package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType

interface QuestTypeOrderSource {

    /** interface to be notified of changes in quest type order */
    interface Listener {
        /** Called when a new order item was added */
        fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType)
        /** Called when the orders changed */
        fun onQuestTypeOrdersChanged()
    }

    /** sort given [questTypes] by user's custom order defined for the given [presetId] (or the
     *  currently selected one if null), if any. */
    fun sort(questTypes: MutableList<QuestType>, presetId: Long? = null)

    /** get the user's custom quest order for the given [presetId] (or the currently selected one
     *  if null) */
    fun getOrders(presetId: Long? = null): List<Pair<QuestType, QuestType>>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
