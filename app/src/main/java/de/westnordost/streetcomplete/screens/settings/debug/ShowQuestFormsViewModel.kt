package de.westnordost.streetcomplete.screens.settings.debug

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.math.translate

abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
    abstract val position: LatLon
    abstract fun createMockElementWithGeometry(questType: OsmElementQuestType<*>): Pair<Element, ElementGeometry>
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

    override fun createMockElementWithGeometry(questType: OsmElementQuestType<*>): Pair<Element, ElementGeometry> {
        val firstPos = position.translate(20.0, 45.0)
        val secondPos = position.translate(20.0, 135.0)
        /* tags are values that results in more that quests working on showing/solving debug quest
           form, i.e. some quests expect specific tags to be set and crash without them - what is
           OK, but here some tag combination needs to be setup to reduce number of crashes when
           using test forms */
        val tags = mapOf(
            "highway" to "cycleway",
            "building" to "residential",
            "name" to "<object name>",
            "opening_hours" to "Mo-Fr 08:00-12:00,13:00-17:30; Sa 08:00-12:00",
            "addr:housenumber" to "176"
        )
        // way geometry is needed by quests using clickable way display (steps direction, sidewalk quest, lane quest, cycleway quest...)
        val element = Way(1, listOf(1, 2), tags, 1)
        val geometry = ElementPolylinesGeometry(listOf(listOf(firstPos, secondPos)), position)
        // for testing quests requiring nodes code above can be commented out and this uncommented
        // val element = Node(1, centerPos, tags, 1)
        // val geometry = ElementPointGeometry(centerPos)
        return element to geometry
    }
}
