package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS

class AddReligionToWaysideShrine : OsmFilterQuestType<Religion>() {

    override val elementFilter = """
        nodes, ways, relations with
          historic = wayside_shrine
          and !religion
          and access !~ private|no
    """
    override val commitMessage = "Add religion for wayside shrine"
    override val wikiLink = "Key:religion"
    override val icon = R.drawable.ic_quest_religion

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_religion_for_wayside_shrine_title

    override fun createForm() = AddReligionForm()

    override fun applyAnswerTo(answer: Religion, changes: StringMapChangesBuilder) {
        changes.add("religion", answer.osmValue)
    }
}
