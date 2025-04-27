package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.overlays.Overlay

/** Configure (edit type preset, selected overlay) through an URL */
class UrlConfigController(
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry,
    private val selectedOverlayController: SelectedOverlayController,
    private val editTypePresetsController: EditTypePresetsController,
    private val visibleEditTypeController: VisibleEditTypeController,
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

        val editTypes = mutableMapOf<EditType, Boolean>()
        editTypes.putAll(questTypeRegistry.map { it to (it in config.questTypes) })
        editTypes.putAll(overlayRegistry.map { it to (it in config.overlays) })
        visibleEditTypeController.setVisibilities(editTypes, presetId)

        questTypeOrderController.setOrders(config.questTypeOrders, presetId)

        // set the current edit type preset + overlay last, so the above do not trigger updates
        editTypePresetsController.selectedId = presetId
        selectedOverlayController.selectedOverlay = config.selectedOverlay
    }

    fun create(presetId: Long): String {
        val urlConfig = UrlConfig(
            presetName = editTypePresetsController.getName(presetId),
            questTypes = visibleEditTypeController.getVisible(presetId).filterIsInstance<QuestType>(),
            questTypeOrders = questTypeOrderController.getOrders(presetId),
            overlays = visibleEditTypeController.getVisible(presetId).filterIsInstance<Overlay>(),
            selectedOverlay = selectedOverlayController.selectedOverlay
        )
        return createConfigUrl(urlConfig, questTypeRegistry, overlayRegistry)
    }
}
