package de.westnordost.streetcomplete.screens.settings.quest_presets

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

abstract class QuestPresetsViewModel : ViewModel() {
    abstract val presets: StateFlow<List<QuestPresetSelection>>

    abstract fun add(name: String)
    abstract fun rename(presetId: Long, name: String)
    abstract fun select(presetId: Long)
    abstract fun duplicate(presetId: Long, name: String)
    abstract fun delete(presetId: Long)

    abstract fun queryUrlConfig(presetId: Long)
}

data class QuestPresetSelection(
    val id: Long,
    val name: String,
    val selected: Boolean,
    val url: String? = null
)

class QuestPresetsViewModelImpl(
    private val questPresetsController: QuestPresetsController,
    private val questTypeOrderController: QuestTypeOrderController,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val urlConfigController: UrlConfigController,
) : QuestPresetsViewModel() {

    override val presets = MutableStateFlow<List<QuestPresetSelection>>(emptyList())

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() {
            val selectedId = questPresetsController.selectedId
            presets.update { presets ->
                presets.map { it.copy(selected = it.id == selectedId) }
            }
        }

        override fun onAddedQuestPreset(preset: QuestPreset) {
            presets.update { presets ->
                presets + QuestPresetSelection(preset.id, preset.name, false)
            }
        }

        override fun onRenamedQuestPreset(preset: QuestPreset) {
            presets.update { presets ->
                presets.map {
                    if (it.id == preset.id) it.copy(name = preset.name, url = null) else it
                }
            }
        }

        override fun onDeletedQuestPreset(presetId: Long) {
            presets.update { presets ->
                presets.filterNot { it.id == presetId }
            }
        }
    }

    init {
        launch(IO) {
            val selectedId = questPresetsController.selectedId
            presets.value = buildList {
                add(QuestPreset(0, ""))
                addAll(questPresetsController.getAll())
            }.map { QuestPresetSelection(it.id, it.name, it.id == selectedId) }
        }
        questPresetsController.addListener(questPresetsListener)
    }

    override fun onCleared() {
        questPresetsController.removeListener(questPresetsListener)
    }

    override fun add(name: String) {
        launch(IO) {
            val newPresetId = questPresetsController.add(name)
            questPresetsController.selectedId = newPresetId
        }
    }

    override fun rename(presetId: Long, name: String) {
        launch(IO) {
            questPresetsController.rename(presetId, name)
        }
    }

    override fun select(presetId: Long) {
        launch(IO) {
            questPresetsController.selectedId = presetId
        }
    }

    override fun duplicate(presetId: Long, name: String) {
        launch(IO) {
            val newPresetId = questPresetsController.add(name)
            questTypeOrderController.copyOrders(presetId, newPresetId)
            visibleQuestTypeController.copyVisibilities(presetId, newPresetId)
            questPresetsController.selectedId = newPresetId
        }
    }

    override fun delete(presetId: Long) {
        launch(IO) {
            questPresetsController.delete(presetId)
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
