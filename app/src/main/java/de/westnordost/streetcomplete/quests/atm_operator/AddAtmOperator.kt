package de.westnordost.streetcomplete.quests.atm_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class AddAtmOperator : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes with amenity = atm and !operator and !name and !brand"
    override val commitMessage = "Add ATM operator"
    override val wikiLink = "Tag:amenity=atm"
    override val icon = R.drawable.ic_quest_money
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_atm_operator_title

    override fun createForm() = AddAtmOperatorForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("operator", answer)
    }
}
