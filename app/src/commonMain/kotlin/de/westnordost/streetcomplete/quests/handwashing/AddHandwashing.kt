package de.westnordost.streetcomplete.quests.handwashing

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddHandwashing : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        amenity = toilets
        and toilets:disposal
        and toilets:disposal != flush
        and !toilets:handwashing
    """
    override val changesetComment = "Survey availability of handwashing capabilites"
    override val wikiLink = "Key:toilets:handwashing"
    override val icon = Res.drawable.quest_washing_hands
    override val title = Res.string.quest_handwashing_title
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    @Composable
    override fun Form(onAnswer: (QuestAnswer<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(onAnswer)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["toilets:handwashing"] = answer.toYesNo()
    }
}
