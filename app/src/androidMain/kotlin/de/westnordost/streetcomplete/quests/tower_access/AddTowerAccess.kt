package de.westnordost.streetcomplete.quests.tower_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddTowerAccess : OsmFilterQuestType<TowerAccess>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with
            man_made = tower
            and tower:type = observation
            and disused != yes
            and !emergency
            and !military
            and (!access or access = unknown)
        """
    override val changesetComment = "Specify access to observation towers"
    override val wikiLink = "Tag:man_made=tower"
    override val icon = R.drawable.ic_quest_tower_access
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tower_access_title

    override fun createForm() = AddTowerAccessForm()

    override fun applyAnswerTo(answer: TowerAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
