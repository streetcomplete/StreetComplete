package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import javax.inject.Inject

/** Provides a list of quest types that are enabled and ordered by (user chosen) importance.
 *
 *  This can be changed anytime by user preference */
class OrderedVisibleQuestTypesProvider @Inject constructor(
        private val questTypeRegistry: QuestTypeRegistry,
        private val visibleQuestTypeDao: VisibleQuestTypeDao,
        private val questTypeOrderList: QuestTypeOrderList
) {
    fun get(): List<QuestType<*>> {
        val visibleQuestTypes = questTypeRegistry.all.mapNotNull { questType ->
            questType.takeIf { visibleQuestTypeDao.isVisible(it) }
        }.toMutableList()

        questTypeOrderList.sort(visibleQuestTypes)

        return visibleQuestTypes
    }
}
/* TODO there should actually be a listener on visible quest types and the quest type order, so that
*  as a response to this, the map display (and other things) can be updated. For example, the
*  visible quest types affects any VisibleQuestListener as well. */
