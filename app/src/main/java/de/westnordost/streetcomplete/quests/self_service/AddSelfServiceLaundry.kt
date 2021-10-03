package de.westnordost.streetcomplete.quests.self_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.*

class AddSelfServiceLaundry : OsmFilterQuestType<SelfServiceLaundry>() {

    override val elementFilter = "nodes, ways with shop = laundry and !self_service"
    override val commitMessage = "Add self service info"
    override val wikiLink = "Tag:shop=laundry"
    override val icon = R.drawable.ic_quest_laundry
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_laundrySelfService_title2

    override fun createForm() = AddSelfServiceLaundryFragment()

    override fun applyAnswerTo(answer: SelfServiceLaundry, changes: StringMapChangesBuilder) {
        when(answer) {
            NO -> {
                changes.add("self_service", "no")
                changes.addOrModify("laundry_service", "yes")
            }
            OPTIONAL -> {
                changes.add("self_service", "yes")
                changes.addOrModify("laundry_service", "yes")
            }
            ONLY -> {
                changes.add("self_service", "yes")
                changes.addOrModify("laundry_service", "no")
            }
        }
    }
}
