package de.westnordost.streetcomplete.quests.map

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class AddMapType : OsmFilterQuestType<MapType>() {

    override val elementFilter = """
        nodes, ways with
          tourism = information
          and information = map
          and !map_type
    """
    override val changesetComment = "Add map type"
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee
    override val wikiLink = "Key:map_type"
    override val icon = R.drawable.ic_quest_map_type
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)
    override fun getTitle(tags: Map<String, String>) = R.string.quest_mapType_title
    override fun applyAnswerTo(answer: MapType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["map_type"] = answer.osmValue
    }

    override fun createForm() = AddMapTypeForm()
}

