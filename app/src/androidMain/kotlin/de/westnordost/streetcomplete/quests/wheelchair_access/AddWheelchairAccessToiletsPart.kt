package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.LIMITED
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.NO
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.YES
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddWheelchairAccessToiletsPart : OsmFilterQuestType<WheelchairAccessToiletsPartAnswer>() {

    override val elementFilter = """
        nodes, ways with
          wheelchair = limited
          and (
           toilets = yes
           or !toilets and (
             amenity ~ restaurant|pub|bar
             or amenity ~ cafe|fast_food and indoor_seating = yes
           )
         )
         and access !~ no|private
         and (
           !toilets:wheelchair
           or toilets:wheelchair != yes and toilets:wheelchair older today -4 years
           or toilets:wheelchair older today -8 years
         )
    """
    override val changesetComment = "Specify wheelchair accessibility of toilets in places"
    override val wikiLink = "Key:toilets:wheelchair"
    override val icon = R.drawable.quest_toilets_wheelchair
    override val title = Res.string.quest_wheelchairAccess_toiletsPart_title2
    override val achievements = listOf(RARE, WHEELCHAIR)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside
    override val hint = Res.string.quest_wheelchairAccess_description_toilets
    override val hintImages = listOf(Res.drawable.wheelchair_sign)

    @Composable
    override fun Form(onAnswer: (WheelchairAccessToiletsPartAnswer) -> Unit, element: Element) {
        QuestForm(
            answers = listOf(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(WheelchairAccessToiletsPart(NO)) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(WheelchairAccessToiletsPart(YES)) },
                Answer(stringResource(Res.string.quest_wheelchairAccess_limited)) { onAnswer(WheelchairAccessToiletsPart(LIMITED)) },
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_wheelchairAccessPat_noToilet)) { onAnswer(WheelchairAccessToiletsPartAnswer.NoToilet) }
            )
        )
    }

    override fun applyAnswerTo(answer: WheelchairAccessToiletsPartAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is WheelchairAccessToiletsPart -> {
                tags.updateWithCheckDate("toilets:wheelchair", answer.access.osmValue)
                if (answer.access != WheelchairAccess.NO) {
                    tags["toilets"] = "yes"
                }
            }
            WheelchairAccessToiletsPartAnswer.NoToilet -> {
                tags.updateWithCheckDate("toilets", "no")
            }
        }
    }
}
