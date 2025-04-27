package de.westnordost.streetcomplete.screens.settings.presets

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Stable
abstract class EditTypePresetsViewModel : ViewModel() {
    abstract val presets: StateFlow<List<EditTypePresetSelection>>

    abstract fun add(name: String)
    abstract fun rename(presetId: Long, name: String)
    abstract fun select(presetId: Long)
    abstract fun duplicate(presetId: Long, name: String)
    abstract fun delete(presetId: Long)

    abstract fun queryUrlConfig(presetId: Long)
}

data class EditTypePresetSelection(
    val id: Long,
    val name: String,
    val selected: Boolean,
    val url: String? = null
)

@Stable
class EditTypePresetsViewModelImpl(
    private val editTypePresetsController: EditTypePresetsController,
    private val questTypeOrderController: QuestTypeOrderController,
    private val visibleEditTypeController: VisibleEditTypeController,
    private val urlConfigController: UrlConfigController,
) : EditTypePresetsViewModel() {

    override val presets = MutableStateFlow<List<EditTypePresetSelection>>(emptyList())

    private val editTypePresetsListener = object : EditTypePresetsSource.Listener {
        override fun onSelectionChanged() {
            val selectedId = editTypePresetsController.selectedId
            presets.update { presets ->
                presets.map { it.copy(selected = it.id == selectedId) }
            }
        }

        override fun onAdded(preset: EditTypePreset) {
            presets.update { presets ->
                presets + EditTypePresetSelection(preset.id, preset.name, false)
            }
        }

        override fun onRenamed(preset: EditTypePreset) {
            presets.update { presets ->
                presets.map {
                    if (it.id == preset.id) it.copy(name = preset.name, url = null) else it
                }
            }
        }

        override fun onDeleted(presetId: Long) {
            presets.update { presets ->
                presets.filterNot { it.id == presetId }
            }
        }
    }

    init {
        launch(IO) {
            val selectedId = editTypePresetsController.selectedId
            presets.value = buildList {
                add(EditTypePreset(0, ""))
                addAll(editTypePresetsController.getAll())
            }.map { EditTypePresetSelection(it.id, it.name, it.id == selectedId) }
        }
        editTypePresetsController.addListener(editTypePresetsListener)
    }

    override fun onCleared() {
        editTypePresetsController.removeListener(editTypePresetsListener)
    }

    override fun add(name: String) {
        launch(IO) {
            val newPresetId = editTypePresetsController.add(name)
            editTypePresetsController.selectedId = newPresetId
        }
    }

    override fun rename(presetId: Long, name: String) {
        launch(IO) {
            editTypePresetsController.rename(presetId, name)
        }
    }

    override fun select(presetId: Long) {
        launch(IO) {
            editTypePresetsController.selectedId = presetId
        }
    }

    override fun duplicate(presetId: Long, name: String) {
        launch(IO) {
            val newPresetId = editTypePresetsController.add(name)
            questTypeOrderController.copyOrders(presetId, newPresetId)
            visibleEditTypeController.copyVisibilities(presetId, newPresetId)
            editTypePresetsController.selectedId = newPresetId
        }
    }

    override fun delete(presetId: Long) {
        launch(IO) {
            editTypePresetsController.delete(presetId)
        }
    }

    override fun queryUrlConfig(presetId: Long) {
        launch(IO) {
            val url = urlConfigController.create(presetId)
            presets.update { presets ->
                presets.map { if (it.id == presetId) it.copy(url = url) else it }
            }
        }
    }
}
