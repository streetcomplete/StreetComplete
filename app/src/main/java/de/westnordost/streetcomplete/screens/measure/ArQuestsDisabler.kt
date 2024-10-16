package de.westnordost.streetcomplete.screens.measure

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.quests.barrier_opening.AddBarrierOpening
import de.westnordost.streetcomplete.quests.max_height.AddMaxPhysicalHeight
import de.westnordost.streetcomplete.quests.width.AddCyclewayWidth
import de.westnordost.streetcomplete.quests.width.AddRoadWidth

class ArQuestsDisabler(
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestTypeController: VisibleQuestTypeController
) {
    private val arQuestNames = listOf(
        AddMaxPhysicalHeight::class.simpleName!!,
        AddRoadWidth::class.simpleName!!,
        AddCyclewayWidth::class.simpleName!!,
        AddBarrierOpening::class.simpleName!!,
    )

    fun hideAllArQuests() {
        val arQuests = arQuestNames.mapNotNull { questTypeRegistry.getByName(it) }
        visibleQuestTypeController.setVisibilities(arQuests.associateWith { false })
    }
}
