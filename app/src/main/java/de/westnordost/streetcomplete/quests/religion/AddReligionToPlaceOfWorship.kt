package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddReligionToPlaceOfWorship : OsmFilterQuestType<Religion>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
            amenity = place_of_worship
            or
            amenity = monastery
        )
        and !religion
    """
    override val changesetComment = "Specify religion for places of worship"
    override val wikiLink = "Key:religion"
    override val icon = R.drawable.ic_quest_religion
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_place_of_worship_title

    override fun createForm() = AddReligionForm()

    override fun applyAnswerTo(answer: Religion, tags: Tags, timestampEdited: Long) {
        tags["religion"] = answer.osmValue
    }
}
