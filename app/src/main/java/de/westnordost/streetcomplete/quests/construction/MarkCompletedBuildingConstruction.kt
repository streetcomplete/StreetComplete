package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.meta.updateCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING

class MarkCompletedBuildingConstruction : OsmFilterQuestType<CompletedConstructionAnswer>() {

    override val elementFilter = """
        ways with building = construction
         and (!opening_date or opening_date < today)
         and older today -6 months
    """
    override val changesetComment = "Determine whether construction is now completed"
    override val wikiLink = "Tag:building=construction"
    override val icon = R.drawable.ic_quest_building_construction

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_construction_building_title

    override fun createForm() = MarkCompletedConstructionForm()

    override fun applyAnswerTo(answer: CompletedConstructionAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is OpeningDateAnswer -> {
                tags["opening_date"] = answer.date.toCheckDateString()
            }
            is StateAnswer -> {
                if (answer.value) {
                    val value = tags["construction"] ?: "yes"
                    tags["building"] = value
                    removeTagsDescribingConstruction(tags)
                } else {
                    tags.updateCheckDate()
                }
            }
        }
    }
}
