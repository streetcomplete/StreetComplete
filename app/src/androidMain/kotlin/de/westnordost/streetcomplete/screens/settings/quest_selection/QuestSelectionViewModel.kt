package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.util.ResourceProvider
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.getIds
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Stable
abstract class QuestSelectionViewModel : ViewModel() {
    abstract val searchText: StateFlow<String>
    abstract val filteredQuests: StateFlow<List<QuestSelection>>
    abstract val currentCountry: String?
    abstract val selectedEditTypePresetName: StateFlow<String?>

    abstract fun select(questType: QuestType, selected: Boolean)
    abstract fun order(questType: QuestType, toAfter: QuestType)
    abstract fun unselectAll()
    abstract fun resetAll()
    abstract fun updateSearchText(text: String)
}

@Stable
class QuestSelectionViewModelImpl(
    private val resourceProvider: ResourceProvider,
    private val questTypeRegistry: QuestTypeRegistry,
    private val editTypePresetsSource: EditTypePresetsSource,
    private val visibleEditTypeController: VisibleEditTypeController,
    private val questTypeOrderController: QuestTypeOrderController,
    countryBoundaries: Lazy<CountryBoundaries>,
    prefs: Preferences,
) : QuestSelectionViewModel() {

    override val searchText = MutableStateFlow("")

    private val questTitles = MutableStateFlow<Map<String, String>>(emptyMap())

    private val visibleEditTypeListener = object : VisibleEditTypeSource.Listener {
        override fun onVisibilityChanged(editType: EditType, visible: Boolean) {
            quests.update { quests ->
                val result = quests.toMutableList()
                val index = result.indexOfFirst { it.questType == editType }
                if (index != -1) {
                    result[index] = result[index].copy(selected = visible)
                }
                return@update result
            }
        }

        // all/many visibilities have changed - re-init list
        override fun onVisibilitiesChanged() { initQuests() }
    }

    private val questTypeOrderListener = object : QuestTypeOrderSource.Listener {
        override fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType) {
            quests.update { quests ->
                val result = quests.toMutableList()
                val itemIndex = result.indexOfFirst { it.questType == item }
                val toAfterIndex = result.indexOfFirst { it.questType == toAfter }

                val questType = result.removeAt(itemIndex)
                result.add(toAfterIndex + if (itemIndex > toAfterIndex) 1 else 0, questType)
                return@update result
            }
        }

        // all/many quest orders have been changed - re-init list
        override fun onQuestTypeOrdersChanged() { initQuests() }
    }

    private val editTypePresetsListener = object : EditTypePresetsSource.Listener {
        override fun onSelectionChanged() { updateSelectedEditTypePresetName() }
        override fun onAdded(preset: EditTypePreset) {}
        override fun onRenamed(preset: EditTypePreset) {}
        override fun onDeleted(presetId: Long) {}
    }

    private val quests = MutableStateFlow<List<QuestSelection>>(emptyList())

    override val filteredQuests: StateFlow<List<QuestSelection>> =
        combine(quests, searchText, questTitles) { quests, searchText, titles ->
            filterQuests(quests, searchText, titles)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val currentCountryCodes = countryBoundaries.value.getIds(prefs.mapPosition)

    override val selectedEditTypePresetName = MutableStateFlow<String?>(null)

    override val currentCountry: String?
        get() = currentCountryCodes.firstOrNull()

    init {
        initQuests()
        updateSelectedEditTypePresetName()
        loadQuestTitles()
        editTypePresetsSource.addListener(editTypePresetsListener)
        visibleEditTypeController.addListener(visibleEditTypeListener)
        questTypeOrderController.addListener(questTypeOrderListener)
    }

    private fun updateSelectedEditTypePresetName() {
        launch(IO) {
            selectedEditTypePresetName.value = editTypePresetsSource.selectedEditTypePresetName
        }
    }

    private fun loadQuestTitles() {
        // This method loads titles only once. When the system language changes, the titles
        // are not reloaded automatically since there is no listenable callback from the
        // system for when the language changes
        launch(Default) {
            questTitles.value = questTypeRegistry
                .associate { it.name to resourceProvider.getString(it.title) }
        }
    }

    override fun onCleared() {
        editTypePresetsSource.removeListener(editTypePresetsListener)
        visibleEditTypeController.removeListener(visibleEditTypeListener)
        questTypeOrderController.removeListener(questTypeOrderListener)
    }

    override fun select(questType: QuestType, selected: Boolean) {
        launch(IO) {
            visibleEditTypeController.setVisibility(questType, selected)
        }
    }

    override fun order(questType: QuestType, toAfter: QuestType) {
        launch(IO) {
            questTypeOrderController.addOrderItem(questType, toAfter)
        }
    }

    override fun unselectAll() {
        launch(IO) {
            visibleEditTypeController.setVisibilities(questTypeRegistry.associateWith { false })
        }
    }

    override fun resetAll() {
        launch(IO) {
            visibleEditTypeController.clearVisibilities(questTypeRegistry)
            questTypeOrderController.clear()
        }
    }

    override fun updateSearchText(text: String) {
        searchText.value = text
    }

    private fun initQuests() {
        launch(IO) {
            val sortedQuestTypes = questTypeRegistry.toMutableList()
            questTypeOrderController.sort(sortedQuestTypes)
            quests.value = sortedQuestTypes
                .map { QuestSelection(
                    questType = it,
                    selected = visibleEditTypeController.isVisible(it),
                    enabledInCurrentCountry = isQuestEnabledInCurrentCountry(it)
                ) }
                .toMutableList()
        }
    }

    private fun isQuestEnabledInCurrentCountry(questType: QuestType): Boolean {
        if (questType !is OsmElementQuestType<*>) return true
        return when (val countries = questType.enabledInCountries) {
            is AllCountries -> true
            is AllCountriesExcept -> !countries.exceptions.containsAny(currentCountryCodes)
            is NoCountriesExcept -> countries.exceptions.containsAny(currentCountryCodes)
        }
    }

    private fun filterQuests(
        quests: List<QuestSelection>,
        filter: String,
        titles: Map<String, String>,
    ): List<QuestSelection> {
        val words = filter.takeIf { it.isNotBlank() }?.trim()?.lowercase()?.split(' ') ?: emptyList()
        return if (words.isEmpty()) {
            quests
        } else {
            quests.filter { quest ->
                titles[quest.questType.name]?.lowercase()?.containsAll(words) == true
            }
        }
    }
}
