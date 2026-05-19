package de.westnordost.streetcomplete.screens.measure

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController

class ArQuestsDisabler(
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleEditTypeController: VisibleEditTypeController,
) {
    private val arQuestNames = listOf(
        // can revert to using AddMaxPhysicalHeight::class.simpleName!! etc. once all the quests
        // are in commonMain
        "AddMaxPhysicalHeight",
        "AddRoadWidth",
        "AddCyclewayWidth",
        "AddBarrierOpening",
    )

    fun hideAllArQuests() {
        val arQuests = arQuestNames.mapNotNull { questTypeRegistry.getByName(it) }
        visibleEditTypeController.setVisibilities(arQuests.associateWith { false })
    }
}
