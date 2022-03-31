package de.westnordost.streetcomplete.quests.air_pump

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBicyclePump : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
         amenity = bicycle_repair_station or shop = bicycle
         and !compressed_air
         and !service:bicycle:pump
    """
    override val changesetComment = "Add whether bicycle pump is available"
    override val wikiLink = "Key:service:bicycle:pump"
    override val icon = R.drawable.ic_quest_air_pump
    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_air_pump_bicycle_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways with compressed_air or amenity = compressed_air or service:bicycle:pump")

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["service:bicycle:pump"] = answer.toYesNo()
    }
}
