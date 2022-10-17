package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.overlays.Overlay

/** Configure (quest preset, selected overlay) through an URL */
class UrlConfigController(
    private val questTypeRegistry: QuestTypeRegistry,
    private val selectedOverlayController: SelectedOverlayController,
    private val questPresetsController: QuestPresetsController,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val questTypeOrderController: QuestTypeOrderController
) {
    fun apply(config: UrlConfig) {
        // TODO what if preset by that name already exists?
        val presetId = questPresetsController.add(config.presetName)

        val questTypes = questTypeRegistry.associateWith { it in config.questTypes }
        visibleQuestTypeController.setVisibilities(questTypes, presetId)
        questPresetsController.selectedId = presetId
        selectedOverlayController.selectedOverlay = config.overlay
    }

    fun get(presetId: Long, overlay: Overlay?): UrlConfig {
        return UrlConfig(
            presetName = questPresetsController.getName(presetId) ?: "Default",
            questTypes = visibleQuestTypeController.getVisible(presetId),
            questTypeOrders = emptyList(),
            overlay = overlay
        )
    }
}
