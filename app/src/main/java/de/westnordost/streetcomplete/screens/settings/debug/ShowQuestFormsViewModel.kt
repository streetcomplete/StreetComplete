package de.westnordost.streetcomplete.screens.settings.debug

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.map.MapStateStore
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
    abstract val position: LatLon
}

class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry,
    private val mapStateStore: MapStateStore,
) : ShowQuestFormsViewModel() {
    override val quests get() = questTypeRegistry
    override val position get() = mapStateStore.position
}
