package de.westnordost.streetcomplete.quests.fuel_service

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddFuelSelfService : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
          amenity = fuel
          and !self_service
          and !automated
    """
    override val changesetComment = "Add self service information to fuel station"
    override val wikiLink = "Key:self_service"
    override val icon = R.drawable.ic_quest_fuel_self_service

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val hasBrand = tags.containsKey("brand")
        return when {
            hasName || hasBrand ->  R.string.quest_fuelSelfService_name_title
            else ->                 R.string.quest_fuelSelfService_title
        }
    }

    override val enabledInCountries = NoCountriesExcept("IT", "UK")

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["self_service"] = answer.toYesNo()
    }
}
