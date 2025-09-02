package de.westnordost.streetcomplete.quests.handwashing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddHandwashing : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
        amenity = toilets
        and toilets:disposal
        and toilets:disposal != flush
        and !toilets:handwashing
    """
    override val changesetComment = "Survey availability of handwashing capabilites"
    override val wikiLink = "Key:toilets:handwashing"
    override val icon = R.drawable.ic_quest_washing_hands
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_handwashing_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["toilets:handwashing"] = answer.toYesNo()
    }
}
