package de.westnordost.streetcomplete.quests.toilets_fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.util.countryboundaries.AllCountriesExcept
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.stringResource

class AddToiletsFee : OsmFilterQuestType<ToiletFeeAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = toilets
          and access !~ private|customers
          and !fee and !fee:conditional
          and (!seasonal or seasonal = no)
    """
    override val changesetComment = "Specify toilet fees"
    override val wikiLink = "Key:fee"
    override val icon = Res.drawable.quest_toilet_fee
    override val title = Res.string.quest_toiletsFee_title
    // countries where it is either illegal to charge a fee or changing a fee is a non-existent concept
    override val enabledInCountries = AllCountriesExcept("US", "CA", "AU", "NZ")
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<ToiletFeeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(ToiletFee(false))) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(ToiletFee(true))) },
            ),
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_toiletsFee_fee_for_non_customers)) {
                    on(Answer(ToiletFeeForNonCustomers))
                },
            ) }
        )

    }

    override fun applyAnswerTo(answer: ToiletFeeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.remove("fee:conditional")
        when (answer) {
            is ToiletFee -> tags["fee"] = answer.fee.toYesNo()
            is ToiletFeeForNonCustomers -> {
                tags["fee"] = "yes"
                tags["fee:conditional"] = "no @ customers"
            }
        }
    }
}
