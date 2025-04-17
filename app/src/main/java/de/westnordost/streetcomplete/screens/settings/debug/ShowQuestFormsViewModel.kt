package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ResourceProvider
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Stable
abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
    abstract val searchText: StateFlow<String>
    abstract val filteredQuests: StateFlow<List<QuestType>>

    abstract fun updateSearchText(text: String)
}

@Stable
class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry,
    private val resourceProvider: ResourceProvider,
) : ShowQuestFormsViewModel() {
    override val quests get() = questTypeRegistry
    override val searchText = MutableStateFlow("")

    private val questTitles = MutableStateFlow<Map<String, String>>(emptyMap())

    override val filteredQuests: StateFlow<List<QuestType>> =
        combine(searchText, questTitles) { searchText, titles ->
            filterQuests(quests, searchText, titles)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override fun updateSearchText(text: String) {
        searchText.value = text
    }

    init {
        loadQuestTitles()
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

    private fun filterQuests(
        quests: List<QuestType>,
        filter: String,
        titles: Map<String, String>,
    ): List<QuestType> {
        val words =
            filter.takeIf { it.isNotBlank() }?.trim()?.lowercase()?.split(' ') ?: emptyList()
        return if (words.isEmpty()) {
            quests
        } else {
            quests.filter { quest ->
                titles[quest.name]?.lowercase()?.containsAll(words) == true
            }
        }
    }
}
