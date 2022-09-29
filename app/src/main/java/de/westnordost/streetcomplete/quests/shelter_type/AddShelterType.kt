package de.westnordost.streetcomplete.quests.shelter_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddShelterType : OsmFilterQuestType<ShelterType>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity=shelter
          and !shelter_type
    """
    override val changesetComment = "Specify shelter types"
    override val wikiLink = "Key:shelter_type"
    override val icon = R.drawable.ic_quest_shelter_type
    override val isDeleteElementEnabled = true
    override val achievements = listOf(EditTypeAchievement.OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shelterType_title

    override fun createForm() = AddShelterTypeForm()

    override fun applyAnswerTo(answer: ShelterType, tags: Tags, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
