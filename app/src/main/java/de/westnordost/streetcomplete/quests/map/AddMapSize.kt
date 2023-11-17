package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddMapSize : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          tourism = information
          and information = map
          and !map_size
    """
    override val changesetComment = "Add what area a map covers"
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee
    override val wikiLink = "Key:map_size"
    override val icon = R.drawable.ic_quest_map_size
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override fun getTitle(tags: Map<String, String>) = R.string.quest_mapSize_title

    override fun createForm() = AddMapSizeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["map_size"] = answer
    }
}
