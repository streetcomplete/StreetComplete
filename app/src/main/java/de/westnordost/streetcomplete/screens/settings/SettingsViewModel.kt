package de.westnordost.streetcomplete.screens.settings

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.get
import com.russhwolf.settings.set
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

    abstract val resurveyIntervals: StateFlow<Prefs.ResurveyIntervals>
    abstract val showAllNotes: StateFlow<Boolean>
    abstract val autosync: StateFlow<Prefs.Autosync>
    abstract val theme: StateFlow<Prefs.Theme>
    abstract val keepScreenOn: StateFlow<Boolean>
    abstract val selectedLanguage: StateFlow<String>

    abstract fun unhideQuests()

    abstract fun deleteCache()

    abstract fun setResurveyIntervals(value: Prefs.ResurveyIntervals)
    abstract fun setShowAllNotes(value: Boolean)
    abstract fun setAutosync(value: Prefs.Autosync)
    abstract fun setTheme(value: Prefs.Theme)
    abstract fun setKeepScreenOn(value: Boolean)
    abstract fun setSelectedLanguage(value: String)
}

data class QuestTypeCount(val total: Int, val enabled: Int)

class SettingsViewModelImpl(
    private val prefs: ObservableSettings,
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

    override val resurveyIntervals = MutableStateFlow(
        Prefs.ResurveyIntervals.of(prefs.getStringOrNull(Prefs.RESURVEY_INTERVALS))
    )
    override val autosync = MutableStateFlow(
        Prefs.Autosync.of(prefs.getStringOrNull(Prefs.AUTOSYNC))
    )
    override val theme = MutableStateFlow(
        Prefs.Theme.of(prefs.getStringOrNull(Prefs.THEME_SELECT))
    )
    override val showAllNotes = MutableStateFlow(
        prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
    )
    override val keepScreenOn = MutableStateFlow(
        prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false)
    )
    override val selectedLanguage = MutableStateFlow(
        prefs.getString(Prefs.LANGUAGE_SELECT, "")
    )

    private val listeners = mutableListOf<SettingsListener>()

    init {
        visibleQuestTypeSource.addListener(visibleQuestTypeListener)
        questPresetsSource.addListener(questPresetsListener)
        osmNoteQuestsHiddenController.addListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.addListener(osmQuestsHiddenListener)

        listeners += prefs.addStringOrNullListener(Prefs.RESURVEY_INTERVALS) {
            resurveyIntervals.value = Prefs.ResurveyIntervals.of(it)
        }
        listeners += prefs.addStringOrNullListener(Prefs.AUTOSYNC) {
            autosync.value = Prefs.Autosync.of(it)
        }
        listeners += prefs.addStringOrNullListener(Prefs.THEME_SELECT) {
            theme.value = Prefs.Theme.of(it)
        }
        listeners += prefs.addBooleanListener(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false) {
            showAllNotes.value = it
        }
        listeners += prefs.addBooleanListener(Prefs.KEEP_SCREEN_ON, false) {
            keepScreenOn.value = it
        }
        listeners += prefs.addStringOrNullListener(Prefs.LANGUAGE_SELECT) {
            selectedLanguage.value = it ?: ""
        }

        updateQuestTypeCount()
        updateSelectableLanguageCodes()
        updateHiddenQuests()
        updateSelectedQuestPreset()
    }

    override fun onCleared() {
        questPresetsSource.removeListener(questPresetsListener)
        osmNoteQuestsHiddenController.removeListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.removeListener(osmQuestsHiddenListener)

        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    override fun deleteCache() {
        cleaner.cleanAll()
    }

    override fun setResurveyIntervals(value: Prefs.ResurveyIntervals) {
        prefs[Prefs.RESURVEY_INTERVALS] = value.name
    }

    override fun setShowAllNotes(value: Boolean) {
        prefs[Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS] = value
    }

    override fun setAutosync(value: Prefs.Autosync) {
        prefs[Prefs.AUTOSYNC] = value.name
    }

    override fun setTheme(value: Prefs.Theme) {
        prefs[Prefs.THEME_SELECT] = value.name
    }

    override fun setKeepScreenOn(value: Boolean) {
        prefs[Prefs.KEEP_SCREEN_ON] = value
    }

    override fun setSelectedLanguage(value: String) {
        prefs[Prefs.LANGUAGE_SELECT] = value
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
