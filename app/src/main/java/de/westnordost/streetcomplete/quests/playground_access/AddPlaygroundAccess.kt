package de.westnordost.streetcomplete.quests.playground_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class AddPlaygroundAccess : OsmFilterQuestType<PlaygroundAccess>() {

    override val elementFilter = "nodes, ways, relations with leisure = playground and (!access or access = unknown)"
    override val changesetComment = "Add playground access"
    override val wikiLink = "Tag:leisure=playground"
    override val icon = R.drawable.ic_quest_playground

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_playground_access_title

    override fun createForm() = AddPlaygroundAccessForm()

    override fun applyAnswerTo(answer: PlaygroundAccess, tags: Tags, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
