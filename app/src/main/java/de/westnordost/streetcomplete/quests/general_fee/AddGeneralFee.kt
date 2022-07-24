package de.westnordost.streetcomplete.quests.general_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddGeneralFee : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
         (tourism = museum or leisure = beach_resort or tourism = gallery)
         and access !~ private|no
         and !fee
    """
    override val changesetComment = "Specify whether places take fees to visit"
    override val wikiLink = "Key:fee"
    override val icon = R.drawable.ic_quest_fee
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_generalFee_title2

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
}
