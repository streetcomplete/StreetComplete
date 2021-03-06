package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controls access to VisibleQuestTypeDao and  */
@Singleton class VisibleQuestTypeController @Inject constructor(
    private val db: VisibleQuestTypeDao
): VisibleQuestTypeSource {

    /* Is a singleton because it has a in-memory cache that is synchronized with changes made on
       the DB and because it notifies listeners */

    private val cache: MutableMap<String, Boolean> by lazy { db.getAll() }

    private val listeners: MutableList<VisibleQuestTypeSource.Listener> = CopyOnWriteArrayList()

    @Synchronized override fun isVisible(questType: QuestType<*>): Boolean {
        val questTypeName = questType::class.simpleName!!
        return cache[questTypeName] ?: (questType.defaultDisabledMessage <= 0)
    }

    @Synchronized fun setVisible(questType: QuestType<*>, visible: Boolean) {
        val questTypeName = questType::class.simpleName!!
        db.put(questTypeName, visible)
        cache[questTypeName] = visible
        listeners.forEach { it.onQuestTypeVisibilitiesChanged() }
    }

    @Synchronized fun clear() {
        db.clear()
        cache.clear()
        listeners.forEach { it.onQuestTypeVisibilitiesChanged() }
    }

    override fun addListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: VisibleQuestTypeSource.Listener) {
        listeners.remove(listener)
    }
}
