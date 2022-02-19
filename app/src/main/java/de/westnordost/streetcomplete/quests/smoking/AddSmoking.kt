package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddSmoking : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
          amenity ~ bar|cafe|pub|biergarten|restaurant|nightclub|stripclub
          or leisure ~ outdoor_seating
        )
        and !smoking
    """
    override val changesetComment = "Add smoking status"
    override val wikiLink = "Key:smoking"
    override val icon = R.drawable.ic_quest_smoke
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_regional_warning

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        if (hasFeatureName(tags))
            R.string.quest_smoking_name_type_title
        else
            R.string.quest_smoking_no_name_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        return arrayOfNotNull(name, featureName.value)
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["smoking"] = answer.toYesNo()
    }

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        tags.containsKey("name") || tags.containsKey("brand")
}
