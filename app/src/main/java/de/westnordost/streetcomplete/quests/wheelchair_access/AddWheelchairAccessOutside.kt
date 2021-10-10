package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.RARE
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR

class AddWheelchairAccessOutside : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways, relations with leisure = dog_park
         and access !~ no|private
         and (!wheelchair or wheelchair older today -8 years)
    """
    override val commitMessage = "Add wheelchair access to outside places"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair

    override val questTypeAchievements = listOf(RARE, WHEELCHAIR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_outside_title

    override fun createForm() = AddWheelchairAccessOutsideForm()

    override fun applyAnswerTo(answer: WheelchairAccess, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("wheelchair", answer.osmValue)
    }
}
