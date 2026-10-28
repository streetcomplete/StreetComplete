package de.westnordost.streetcomplete.quests.payment

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
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept
import de.westnordost.streetcomplete.util.ktx.toYesNo
import de.westnordost.streetcomplete.quests.payment.PaymentUtils

class AddAcceptsCash : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
          ${PaymentUtils.commonFilter}
          and (
            !payment:cash and !payment:coins and !payment:notes and payment:others != no
            or (
              payment:cash = yes and payment:cash older today -2 years
              or payment:cash older today -4 years
            )
          )
    """

    override val changesetComment = "Survey whether payment with cash is accepted"
    override val wikiLink = "Key:payment"
    override val icon = Res.drawable.quest_cash
    override val title = Res.string.quest_accepts_cash_title2
    override val enabledInCountries = NoCountriesExcept(
        "FI", // https://github.com/streetcomplete/StreetComplete/issues/5500
        "GB", // https://github.com/streetcomplete/StreetComplete/issues/4517
        "IS", // https://github.com/streetcomplete/StreetComplete/issues/6897
        "SE",
        "NL", // https://github.com/streetcomplete/StreetComplete/issues/4826
    )
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = Res.string.default_disabled_msg_go_inside

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<Boolean>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        YesNoQuestForm(on)
    }

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("payment:cash", answer.toYesNo())
    }
}
