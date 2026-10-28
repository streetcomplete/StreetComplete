package de.westnordost.streetcomplete.quests.payment

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.stringResource

class AddAcceptsCards : OsmFilterQuestType<CardAcceptance>() {

    override val elementFilter = """
        nodes, ways, relations with
          ${PaymentUtils.commonFilter}
          and (
            !payment:credit_cards and !payment:debit_cards and payment:others != no
            or payment:debit_cards older today -4 years
            or payment:credit_cards older today -4 years
          )
          and !brand and !wikipedia:brand and !wikidata:brand
    """
    override val changesetComment = "Survey whether payment with cards is accepted"
    override val wikiLink = "Key:payment"
    override val icon = Res.drawable.quest_card
    override val title = Res.string.quest_accepts_cards
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<CardAcceptance>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        RadioGroupQuestForm(
            on = on,
            items = CardAcceptance.entries,
            itemContent = { Text(stringResource(it.text)) },
        )
    }

    override fun applyAnswerTo(answer: CardAcceptance, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("payment:debit_cards", answer.debit.toYesNo())
        tags.updateWithCheckDate("payment:credit_cards", answer.credit.toYesNo())
    }
}
