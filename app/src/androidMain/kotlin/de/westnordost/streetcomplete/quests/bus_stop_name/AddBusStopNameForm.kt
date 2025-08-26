package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.confirmNoName
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo
import org.koin.android.ext.android.inject

class AddBusStopNameForm : AAddLocalizedNameForm<BusStopNameAnswer>() {

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) {
            confirmNoName(requireContext()) { applyAnswer(BusStopNameAnswer.NoName) }
        },
        AnswerItem(R.string.quest_streetName_answer_cantType) {
            showKeyboardInfo(requireContext())
        }
    )

    // this filter needs to be kept somewhat in sync with the filter in AddBusStopName
    private val busStopsWithNamesFilter = """
        nodes, ways, relations with
        (
          public_transport = platform and bus = yes
          or highway = bus_stop and public_transport != stop_position
          or railway ~ halt|station|tram_stop
        )
        and name
    """.toElementFilterExpression()

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        nameSuggestionsSource.getNames(position, clickAreaSizeInMeters, busStopsWithNamesFilter)
            .firstOrNull()
            ?.let { localizedNames.value = it }

        return true
    }

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(BusStopName(names))
    }
}
