package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.getString

@Stable
abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val searchText: StateFlow<String>
    abstract val filteredQuests: StateFlow<List<QuestType>>

    abstract fun updateSearchText(text: String)

    abstract val mockElement: Element
    abstract val mockGeometry: ElementGeometry
    abstract val mockRotation: Float
}

@Stable
class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry,
    private val prefs: Preferences,
) : ShowQuestFormsViewModel() {
    override val searchText = MutableStateFlow("")

    private val questTitles = MutableStateFlow<Map<String, String>>(emptyMap())

    private val mockPosition get() = prefs.mapPosition

    override val filteredQuests: StateFlow<List<QuestType>> =
        combine(searchText, questTitles) { searchText, titles ->
            filterQuests(questTypeRegistry, searchText, titles)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override fun updateSearchText(text: String) {
        searchText.value = text
    }

    override val mockRotation: Float = 30f
    override val mockElement: Element
    override val mockGeometry: ElementGeometry

    init {
        loadQuestTitles()

        mockElement = createMockElement()
        mockGeometry = createMockGeometry()
    }

    private fun createMockElement(): Element {
        /* we use some set of tags with which most quests will not protest (i.e. crash ;-)) when
         *  they have to handle them */
        val tags = mapOf(
            "highway" to "cycleway",
            "building" to "residential",
            "name" to "<object name>",
            "opening_hours" to "Mo-Fr 08:00-12:00,13:00-17:30; Sa 08:00-12:00",
            "addr:housenumber" to "176"
        )
        /* we use way geometry because some quests require way geometry (e.g. steps direction,
           sidewalk quest, lane quest, cycleway quest...) while most don't care */
        val element = Way(1, listOf(1, 2), tags, 1)

        // for testing quests requiring nodes code above can be commented out and this uncommented
        // val element = Node(1, mockPosition, tags, 1)

        return element
    }

    private fun createMockGeometry(): ElementGeometry {
        /* we use way geometry because some quests require way geometry (e.g. steps direction,
           sidewalk quest, lane quest, cycleway quest...) while most don't care */
        val position = mockPosition
        val firstPos = position.translate(20.0, mockRotation.toDouble())
        val secondPos = position.translate(20.0, 180.0 - mockRotation.toDouble())
        val geometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), position)

        // for testing quests requiring nodes code above can be commented out and this uncommented
        // val geometry = ElementPointGeometry(position)

        return geometry
    }

    private fun loadQuestTitles() {
        // This method loads titles only once. When the system language changes, the titles
        // are not reloaded automatically since there is no listenable callback from the
        // system for when the language changes
        launch(Default) {
            questTitles.value = questTypeRegistry.associate { it.name to getString(it.title) }
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
