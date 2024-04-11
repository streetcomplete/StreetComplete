package de.westnordost.streetcomplete.screens.settings

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsViewModel : ViewModel() {
    abstract val selectableLanguageCodes: StateFlow<List<String>?>
    abstract val selectedQuestPresetName: StateFlow<String?>
    abstract val hiddenQuestCount: StateFlow<Long>
    abstract val questTypeCount: StateFlow<QuestTypeCount?>
    abstract val tileCacheSize: StateFlow<Int>

    abstract fun unhideQuests()

    abstract fun deleteCache()

    /* this direct access should be removed in the mid-term. However, since the
     * PreferenceFragmentCompat already implicitly accesses the shared preferences to display the
     * current choice, the ViewModel needs to be adapted anyway later when the view does not
     * inherit from that construct anymore and include many more StateFlows based off the
     * Preferences displayed here -  */
    abstract val prefs: ObservableSettings
}

data class QuestTypeCount(val total: Int, val enabled: Int)

class SettingsViewModelImpl(
    override val prefs: ObservableSettings,
    private val resources: Resources,
    private val cleaner: Cleaner,
    private val osmQuestsHiddenController: OsmQuestsHiddenController,
    private val osmNoteQuestsHiddenController: OsmNoteQuestsHiddenController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val questPresetsSource: QuestPresetsSource,
) : SettingsViewModel() {

    private val visibleQuestTypeListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) { updateQuestTypeCount() }
        override fun onQuestTypeVisibilitiesChanged() { updateQuestTypeCount() }
    }

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { updateSelectedQuestPreset() }
        override fun onAddedQuestPreset(preset: QuestPreset) { updateSelectedQuestPreset() }
        override fun onRenamedQuestPreset(preset: QuestPreset) { updateSelectedQuestPreset() }
        override fun onDeletedQuestPreset(presetId: Long) { updateSelectedQuestPreset() }
    }

    private val osmQuestsHiddenListener = object : OsmQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmQuestHidden) { updateHiddenQuests() }
        override fun onUnhid(edit: OsmQuestHidden) { updateHiddenQuests() }
        override fun onUnhidAll() { updateHiddenQuests() }
    }

    private val osmNoteQuestsHiddenListener = object : OsmNoteQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmNoteQuestHidden) { updateHiddenQuests() }
        override fun onUnhid(edit: OsmNoteQuestHidden) { updateHiddenQuests() }
        override fun onUnhidAll() { updateHiddenQuests() }
    }

    override val hiddenQuestCount = MutableStateFlow(0L)
    override val questTypeCount = MutableStateFlow<QuestTypeCount?>(null)
    override val selectedQuestPresetName = MutableStateFlow<String?>(null)
    override val selectableLanguageCodes = MutableStateFlow<List<String>?>(null)
    override val tileCacheSize = MutableStateFlow(prefs.getInt(
        Prefs.MAP_TILECACHE_IN_MB,
        ApplicationConstants.DEFAULT_MAP_CACHE_SIZE_IN_MB
    ))

    private val listeners = mutableListOf<SettingsListener>()

    init {
        visibleQuestTypeSource.addListener(visibleQuestTypeListener)
        questPresetsSource.addListener(questPresetsListener)
        osmNoteQuestsHiddenController.addListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.addListener(osmQuestsHiddenListener)

        listeners += prefs.addIntOrNullListener(Prefs.MAP_TILECACHE_IN_MB) { size ->
            tileCacheSize.value = size ?: ApplicationConstants.DEFAULT_MAP_CACHE_SIZE_IN_MB
        }

        updateSelectableLanguageCodes()
        updateHiddenQuests()
        updateSelectedQuestPreset()
        updateQuestTypeCount()
    }

    override fun onCleared() {
        visibleQuestTypeSource.removeListener(visibleQuestTypeListener)
        questPresetsSource.removeListener(questPresetsListener)
        osmNoteQuestsHiddenController.removeListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.removeListener(osmQuestsHiddenListener)

        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    override fun deleteCache() {
        cleaner.cleanAll()
    }

    override fun unhideQuests() {
        launch(IO) {
            osmQuestsHiddenController.unhideAll()
            osmNoteQuestsHiddenController.unhideAll()
        }
    }

    private fun updateSelectedQuestPreset() {
        launch(IO) {
            selectedQuestPresetName.value = questPresetsSource.selectedQuestPresetName
        }
    }

    private fun updateSelectableLanguageCodes() {
        launch(IO) {
            selectableLanguageCodes.value = resources.getYamlObject<List<String>>(R.raw.languages)
        }
    }

    private fun updateHiddenQuests() {
        launch(IO) {
            hiddenQuestCount.value =
                osmQuestsHiddenController.countAll() + osmNoteQuestsHiddenController.countAll()
        }
    }

    private fun updateQuestTypeCount() {
        launch(IO) {
            questTypeCount.value = QuestTypeCount(
                total = questTypeRegistry.size,
                enabled = questTypeRegistry.count { visibleQuestTypeSource.isVisible(it) }
            )
        }
    }
}
