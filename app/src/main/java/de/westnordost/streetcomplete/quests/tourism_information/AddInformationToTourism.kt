package de.westnordost.streetcomplete.quests.tourism_information

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags

class AddInformationToTourism : OsmFilterQuestType<TourismInformation>() {

    override val elementFilter = "nodes, ways with tourism = information and !information"
    override val changesetComment = "Specify type of tourist informations"
    override val wikiLink = "Tag:tourism=information"
    override val icon = R.drawable.ic_quest_information
    override val isDeleteElementEnabled = true
    override val achievements = listOf(RARE, CITIZEN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tourism_information_title

    override fun createForm() = AddInformationForm()

    override fun applyAnswerTo(answer: TourismInformation, tags: Tags, timestampEdited: Long) {
        tags["information"] = answer.osmValue
    }
}
