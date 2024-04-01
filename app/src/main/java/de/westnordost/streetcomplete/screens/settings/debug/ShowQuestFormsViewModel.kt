package de.westnordost.streetcomplete.screens.settings.debug

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
    abstract val position: LatLon
}

class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry,
    private val prefs: ObservableSettings,
) : ShowQuestFormsViewModel() {
    override val quests get() = questTypeRegistry
    override val position get() = LatLon(
        prefs.getDouble(Prefs.MAP_LATITUDE, 0.0),
        prefs.getDouble(Prefs.MAP_LONGITUDE, 0.0)
    )
}
