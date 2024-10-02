package de.westnordost.streetcomplete.quests.valves

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags

class AddValves : OsmFilterQuestType<List<Valves>>() {

    override val elementFilter = """
        nodes, ways with
          (compressed_air = yes
          or service:bicycle:pump = yes
          or amenity = compressed_air)
          and access !~ private|no
          and !valves
    """
    override val changesetComment = "Specify valves types for air pumps or compressed air"
    override val wikiLink = "Key:valves"
    override val icon = R.drawable.ic_quest_valve
    override val isDeleteElementEnabled = true
    override val achievements = listOf(BICYCLIST)
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_valves_title

    override fun createForm() = AddValvesForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with amenity = compressed_air or service:bicycle:pump = yes or compressed_air = yes")

    override fun applyAnswerTo(answer: List<Valves>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["valves"] = answer.joinToString(";") { it.osmValue }
    }
}
