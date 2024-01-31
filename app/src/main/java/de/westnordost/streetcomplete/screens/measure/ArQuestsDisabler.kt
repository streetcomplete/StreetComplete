package de.westnordost.streetcomplete.screens.measure

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController

class ArQuestsDisabler(
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestTypeController: VisibleQuestTypeController
) {
    private val arQuestNames = listOf(
        "AddMaxPhysicalHeight",
        "AddRoadWidth",
        "AddCyclewayWidth"
    )

    fun hideAllArQuests() {
        val arQuests = arQuestNames.mapNotNull { questTypeRegistry.getByName(it) }
        visibleQuestTypeController.setVisibilities(arQuests.associateWith { false })
    }
}
