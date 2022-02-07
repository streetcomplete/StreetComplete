package de.westnordost.streetcomplete.quests.self_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.NO
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.ONLY
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.OPTIONAL

class AddSelfServiceLaundry : OsmFilterQuestType<SelfServiceLaundry>() {

    override val elementFilter = "nodes, ways with shop = laundry and !self_service"
    override val changesetComment = "Add self service info"
    override val wikiLink = "Tag:shop=laundry"
    override val icon = R.drawable.ic_quest_laundry
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_laundrySelfService_title2

    override fun createForm() = AddSelfServiceLaundryForm()

    override fun applyAnswerTo(answer: SelfServiceLaundry, tags: Tags, timestampEdited: Long) {
        when (answer) {
            NO -> {
                tags["self_service"] = "no"
                tags["laundry_service"] = "yes"
            }
            OPTIONAL -> {
                tags["self_service"] = "yes"
                tags["laundry_service"] = "yes"
            }
            ONLY -> {
                tags["self_service"] = "yes"
                tags["laundry_service"] = "no"
            }
        }
    }
}
