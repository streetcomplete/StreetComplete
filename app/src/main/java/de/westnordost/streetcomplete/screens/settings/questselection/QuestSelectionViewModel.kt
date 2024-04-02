package de.westnordost.streetcomplete.screens.settings.questselection

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.ktx.containsAny
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class QuestSelectionViewModel : ViewModel() {
    abstract val selectedQuestPresetName: String?
    abstract val currentCountry: String?
    abstract val quests: StateFlow<List<QuestSelection>>

    abstract fun isQuestEnabledInCurrentCountry(questType: QuestType): Boolean
    abstract fun selectQuest(questType: QuestType, selected: Boolean)
    abstract fun orderQuest(questType: QuestType, toAfter: QuestType)
    abstract fun unselectAllQuests()
    abstract fun resetQuestSelectionsAndOrder()
}

class QuestSelectionViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry,
    private val questPresetsSource: QuestPresetsSource,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val questTypeOrderController: QuestTypeOrderController,
    countryBoundaries: Lazy<CountryBoundaries>,
    prefs: ObservableSettings,
) : QuestSelectionViewModel() {

    private val visibleQuestsListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) {
            quests.update { quests ->
                val result = quests.toMutableList()
                val index = result.indexOfFirst { it.questType == questType }
                result[index] = result[index].copy(selected = visible)
                return@update result
            }
        }

        // all/many visibilities have changed - re-init list
        override fun onQuestTypeVisibilitiesChanged() { initQuests() }
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

    override val quests = MutableStateFlow<List<QuestSelection>>(emptyList())

    private val currentCountryCodes = countryBoundaries.value
        .getIds(prefs.getDouble(Prefs.MAP_LONGITUDE, 0.0), prefs.getDouble(Prefs.MAP_LATITUDE, 0.0))

    override val selectedQuestPresetName: String?
        get() = questPresetsSource.selectedQuestPresetName

    override val currentCountry: String?
        get() = currentCountryCodes.firstOrNull()

    init {
        initQuests()
        visibleQuestTypeController.addListener(visibleQuestsListener)
        questTypeOrderController.addListener(questTypeOrderListener)
    }

    override fun onCleared() {
        visibleQuestTypeController.removeListener(visibleQuestsListener)
        questTypeOrderController.removeListener(questTypeOrderListener)
    }

    override fun isQuestEnabledInCurrentCountry(questType: QuestType): Boolean {
        if (questType !is OsmElementQuestType<*>) return true
        return when (val countries = questType.enabledInCountries) {
            is AllCountries -> true
            is AllCountriesExcept -> !countries.exceptions.containsAny(currentCountryCodes)
            is NoCountriesExcept -> countries.exceptions.containsAny(currentCountryCodes)
        }
    }

    override fun selectQuest(questType: QuestType, selected: Boolean) {
        launch(IO) {
            visibleQuestTypeController.setVisibility(questType, selected)
        }
    }

    override fun orderQuest(questType: QuestType, toAfter: QuestType) {
        launch(IO) {
            questTypeOrderController.addOrderItem(questType, toAfter)
        }
    }

    override fun unselectAllQuests() {
        launch(IO) {
            visibleQuestTypeController.setVisibilities(questTypeRegistry.associateWith { false })
        }
    }

    override fun resetQuestSelectionsAndOrder() {
        launch(IO) {
            visibleQuestTypeController.clearVisibilities()
            questTypeOrderController.clear()
        }
    }

    private fun initQuests() {
        launch(IO) {
            val sortedQuestTypes = questTypeRegistry.toMutableList()
            questTypeOrderController.sort(sortedQuestTypes)
            quests.value = sortedQuestTypes
                .map { QuestSelection(it, visibleQuestTypeController.isVisible(it)) }
                .toMutableList()
        }
    }
}
