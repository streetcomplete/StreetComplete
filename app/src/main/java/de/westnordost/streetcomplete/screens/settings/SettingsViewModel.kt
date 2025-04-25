package de.westnordost.streetcomplete.screens.settings

import android.content.res.Resources
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervals
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class SettingsViewModel : ViewModel() {
    abstract val selectableLanguageCodes: StateFlow<List<String>?>
    abstract val selectedEditTypePresetName: StateFlow<String?>
    abstract val hiddenQuestCount: StateFlow<Int>
    abstract val questTypeCount: StateFlow<QuestTypeCount?>
    abstract val overlayCount: StateFlow<QuestTypeCount?>

    abstract val resurveyIntervals: StateFlow<ResurveyIntervals>
    abstract val showAllNotes: StateFlow<Boolean>
    abstract val autosync: StateFlow<Autosync>
    abstract val theme: StateFlow<Theme>
    abstract val keepScreenOn: StateFlow<Boolean>
    abstract val showZoomButtons: StateFlow<Boolean>
    abstract val selectedLanguage: StateFlow<String?>

    abstract fun unhideQuests()

    abstract fun deleteCache()

    abstract fun setResurveyIntervals(value: ResurveyIntervals)
    abstract fun setShowAllNotes(value: Boolean)
    abstract fun setAutosync(value: Autosync)
    abstract fun setTheme(value: Theme)
    abstract fun setKeepScreenOn(value: Boolean)
    abstract fun setShowZoomButtons(value: Boolean)
    abstract fun setSelectedLanguage(value: String?)
}

data class QuestTypeCount(val total: Int, val enabled: Int)

@Stable
class SettingsViewModelImpl(
    private val prefs: Preferences,
    private val resources: Resources,
    private val cleaner: Cleaner,
    private val hiddenQuestsController: QuestsHiddenController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry,
    private val visibleEditTypeSource: VisibleEditTypeSource,
    private val editTypePresetsSource: EditTypePresetsSource,
) : SettingsViewModel() {

    private val visibleEditTypeListener = object : VisibleEditTypeSource.Listener {
        override fun onVisibilityChanged(editType: EditType, visible: Boolean) {
            if (editType is QuestType) updateQuestTypeCount()
            if (editType is Overlay) updateOverlayCount()
        }
        override fun onVisibilitiesChanged() {
            updateQuestTypeCount()
            updateOverlayCount()
        }
    }

    private val editTypePresetsListener = object : EditTypePresetsSource.Listener {
        override fun onSelectionChanged() { updateSelectedEditTypePreset() }
        override fun onAdded(preset: EditTypePreset) { updateSelectedEditTypePreset() }
        override fun onRenamed(preset: EditTypePreset) { updateSelectedEditTypePreset() }
        override fun onDeleted(presetId: Long) { updateSelectedEditTypePreset() }
    }

    private val hiddenQuestsListener = object : QuestsHiddenSource.Listener {
        override fun onHid(key: QuestKey, timestamp: Long) { updateHiddenQuests() }
        override fun onUnhid(key: QuestKey, timestamp: Long) { updateHiddenQuests() }
        override fun onUnhidAll() { updateHiddenQuests() }
    }

    override val hiddenQuestCount = MutableStateFlow(0)
    override val questTypeCount = MutableStateFlow<QuestTypeCount?>(null)
    override val overlayCount = MutableStateFlow<QuestTypeCount?>(null)
    override val selectedEditTypePresetName = MutableStateFlow<String?>(null)
    override val selectableLanguageCodes = MutableStateFlow<List<String>?>(null)

    override val resurveyIntervals = MutableStateFlow(prefs.resurveyIntervals)
    override val autosync = MutableStateFlow(prefs.autosync)
    override val theme = MutableStateFlow(prefs.theme)
    override val showAllNotes = MutableStateFlow(prefs.showAllNotes)
    override val keepScreenOn = MutableStateFlow(prefs.keepScreenOn)
    override val showZoomButtons = MutableStateFlow(prefs.showZoomButtons)
    override val selectedLanguage = MutableStateFlow(prefs.language)

    private val listeners = mutableListOf<SettingsListener>()

    init {
        visibleEditTypeSource.addListener(visibleEditTypeListener)
        editTypePresetsSource.addListener(editTypePresetsListener)
        hiddenQuestsController.addListener(hiddenQuestsListener)

        listeners += prefs.onResurveyIntervalsChanged { resurveyIntervals.value = it }
        listeners += prefs.onAutosyncChanged { autosync.value = it }
        listeners += prefs.onThemeChanged { theme.value = it }
        listeners += prefs.onAllShowNotesChanged { showAllNotes.value = it }
        listeners += prefs.onKeepScreenOnChanged { keepScreenOn.value = it }
        listeners += prefs.onShowZoomButtonsChanged { showZoomButtons.value = it }
        listeners += prefs.onLanguageChanged { selectedLanguage.value = it }

        updateQuestTypeCount()
        updateOverlayCount()
        updateSelectableLanguageCodes()
        updateHiddenQuests()
        updateSelectedEditTypePreset()
    }

    override fun onCleared() {
        visibleEditTypeSource.removeListener(visibleEditTypeListener)
        editTypePresetsSource.removeListener(editTypePresetsListener)
        hiddenQuestsController.removeListener(hiddenQuestsListener)

        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    override fun deleteCache() {
        cleaner.cleanAll()
    }

    override fun setResurveyIntervals(value: ResurveyIntervals) { prefs.resurveyIntervals = value }
    override fun setShowAllNotes(value: Boolean) { prefs.showAllNotes = value }
    override fun setAutosync(value: Autosync) { prefs.autosync = value }
    override fun setTheme(value: Theme) { prefs.theme = value }
    override fun setKeepScreenOn(value: Boolean) { prefs.keepScreenOn = value }
    override fun setShowZoomButtons(value: Boolean) { prefs.showZoomButtons = value }
    override fun setSelectedLanguage(value: String?) { prefs.language = value }

    override fun unhideQuests() {
        launch(IO) {
            hiddenQuestsController.unhideAll()
        }
    }

    private fun updateSelectedEditTypePreset() {
        launch(IO) {
            selectedEditTypePresetName.value = editTypePresetsSource.selectedEditTypePresetName
        }
    }

    private fun updateSelectableLanguageCodes() {
        launch(IO) {
            selectableLanguageCodes.value = resources.getYamlObject<List<String>>(R.raw.languages)
        }
    }

    private fun updateHiddenQuests() {
        launch(IO) {
            hiddenQuestCount.value = hiddenQuestsController.countAll()
        }
    }

    private fun updateQuestTypeCount() {
        launch(IO) {
            questTypeCount.value = QuestTypeCount(
                total = questTypeRegistry.size,
                enabled = questTypeRegistry.count { visibleEditTypeSource.isVisible(it) }
            )
        }
    }

    private fun updateOverlayCount() {
        launch(IO) {
            overlayCount.value = QuestTypeCount(
                total = overlayRegistry.size,
                enabled = overlayRegistry.count { visibleEditTypeSource.isVisible(it) }
            )
        }
    }
}
