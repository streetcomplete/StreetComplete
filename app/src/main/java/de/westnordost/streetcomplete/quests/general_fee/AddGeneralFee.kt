package de.westnordost.streetcomplete.quests.general_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddGeneralFee : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
         (tourism = museum or leisure = beach_resort or tourism = gallery)
         and access !~ private|no
         and !fee
         and name
    """
    override val changesetComment = "Add fee info"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.ic_quest_fee

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_generalFee_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
}
