package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddPlaygroundAccess : OsmFilterQuestType<PlaygroundAccess>() {

    override val elementFilter = "nodes, ways, relations with leisure = playground and (!access or access = unknown)"
    override val commitMessage = "Add playground access"
    override val wikiLink = "Tag:leisure=playground"
    override val icon = R.drawable.ic_quest_playground

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_playground_access_title

    override fun createForm() = AddPlaygroundAccessForm()

    override fun applyAnswerTo(answer: PlaygroundAccess, changes: StringMapChangesBuilder) {
        changes.addOrModify("access", answer.osmValue)
    }
}
