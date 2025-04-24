package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController

/** Configure (edit type preset, selected overlay) through an URL */
class UrlConfigController(
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry,
    private val selectedOverlayController: SelectedOverlayController,
    private val editTypePresetsController: EditTypePresetsController,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val questTypeOrderController: QuestTypeOrderController
) {
    fun parse(url: String): UrlConfig? =
        parseConfigUrl(url, questTypeRegistry, overlayRegistry)

    fun apply(config: UrlConfig) {
        val presetId = if (config.presetName != null) {
            val existingPreset = editTypePresetsController.getByName(config.presetName)
            existingPreset?.id ?: editTypePresetsController.add(config.presetName)
        } else {
            0
        }

        val questTypes = questTypeRegistry.associateWith { it in config.questTypes }
        visibleQuestTypeController.setVisibilities(questTypes, presetId)
        questTypeOrderController.setOrders(config.questTypeOrders, presetId)

        // set the current edit type preset + overlay last, so the above do not trigger updates
        editTypePresetsController.selectedId = presetId
        selectedOverlayController.selectedOverlay = config.overlay
    }

    fun create(presetId: Long): String {
        val urlConfig = UrlConfig(
            presetName = editTypePresetsController.getName(presetId),
            questTypes = visibleQuestTypeController.getVisible(presetId),
            questTypeOrders = questTypeOrderController.getOrders(presetId),
            overlay = selectedOverlayController.selectedOverlay
        )
        return createConfigUrl(urlConfig, questTypeRegistry, overlayRegistry)
    }
}
