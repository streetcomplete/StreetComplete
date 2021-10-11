package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.WHEELCHAIR

class AddWheelchairAccessToilets : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways with amenity = toilets
         and access !~ no|private
         and (
           !wheelchair
           or wheelchair != yes and wheelchair older today -4 years
           or wheelchair older today -8 years
         )
    """
    override val commitMessage = "Add wheelchair access to toilets"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(WHEELCHAIR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_wheelchairAccess_toilets_name_title
        else
            R.string.quest_wheelchairAccess_toilets_title

    override fun createForm() = AddWheelchairAccessToiletsForm()

    override fun applyAnswerTo(answer: WheelchairAccess, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("wheelchair", answer.osmValue)
    }
}
