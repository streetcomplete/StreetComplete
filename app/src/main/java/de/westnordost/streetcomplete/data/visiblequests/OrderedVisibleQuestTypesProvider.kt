package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import javax.inject.Inject

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
