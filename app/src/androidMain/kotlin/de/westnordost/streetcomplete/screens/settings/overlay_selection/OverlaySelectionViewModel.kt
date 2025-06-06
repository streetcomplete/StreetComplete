package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Stable
abstract class OverlaySelectionViewModel : ViewModel() {
    abstract val overlays: StateFlow<List<OverlaySelection>>
    abstract val selectedEditTypePresetName: StateFlow<String?>

    abstract fun select(overlay: Overlay, selected: Boolean)
    abstract fun resetAll()
}

@Stable
class OverlaySelectionViewModelImpl(
    private val overlayRegistry: OverlayRegistry,
    private val editTypePresetsSource: EditTypePresetsSource,
    private val visibleEditTypeController: VisibleEditTypeController,
) : OverlaySelectionViewModel() {

    private val visibleEditTypeListener = object : VisibleEditTypeSource.Listener {
        override fun onVisibilityChanged(editType: EditType, visible: Boolean) {
            overlays.update { overlays ->
                val result = overlays.toMutableList()
                val index = result.indexOfFirst { it.overlay == editType }
                if (index != -1) {
                    result[index] = result[index].copy(selected = visible)
                }
                return@update result
            }
        }

        // all/many visibilities have changed - re-init list
        override fun onVisibilitiesChanged() { initOverlays() }
    }

    private val editTypePresetsListener = object : EditTypePresetsSource.Listener {
        override fun onSelectionChanged() { updateSelectedEditTypePresetName() }
        override fun onAdded(preset: EditTypePreset) {}
        override fun onRenamed(preset: EditTypePreset) {}
        override fun onDeleted(presetId: Long) {}
    }

    override val overlays = MutableStateFlow<List<OverlaySelection>>(emptyList())

    override val selectedEditTypePresetName = MutableStateFlow<String?>(null)

    init {
        initOverlays()
        updateSelectedEditTypePresetName()
        editTypePresetsSource.addListener(editTypePresetsListener)
        visibleEditTypeController.addListener(visibleEditTypeListener)
    }

    private fun updateSelectedEditTypePresetName() {
        launch(IO) {
            selectedEditTypePresetName.value = editTypePresetsSource.selectedEditTypePresetName
        }
    }

    override fun onCleared() {
        editTypePresetsSource.removeListener(editTypePresetsListener)
        visibleEditTypeController.removeListener(visibleEditTypeListener)
    }

    override fun select(overlay: Overlay, selected: Boolean) {
        launch(IO) {
            visibleEditTypeController.setVisibility(overlay, selected)
        }
    }

    override fun resetAll() {
        launch(IO) {
            visibleEditTypeController.clearVisibilities(overlayRegistry)
        }
    }

    private fun initOverlays() {
        launch(IO) {
            overlays.value = overlayRegistry.map {
                OverlaySelection(it, visibleEditTypeController.isVisible(it))
            }
        }
    }
}
