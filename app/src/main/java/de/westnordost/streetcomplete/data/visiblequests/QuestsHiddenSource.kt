package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestKey

interface QuestsHiddenSource {

    interface Listener {
        fun onHid(key: QuestKey, timestamp: Long)
        fun onUnhid(key: QuestKey, timestamp: Long)
        fun onUnhidAll()
    }

    /** Returns the timestamp at which the given quest was hidden by the user, if it was hidden */
    fun get(key: QuestKey): Long?

    /** Get all pairs of quests+timestamp hidden by the user after the given [timestamp] */
    fun getAllNewerThan(timestamp: Long): List<Pair<QuestKey, Long>>

    /** Get number of quests hidden by the user */
    fun countAll(): Int

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
