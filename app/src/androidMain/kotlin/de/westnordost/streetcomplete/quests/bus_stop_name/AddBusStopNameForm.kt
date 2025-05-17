package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.databinding.QuestLocalizednameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.NameSuggestionsSource
import org.koin.android.ext.android.inject

class AddBusStopNameForm : AAddLocalizedNameForm<BusStopNameAnswer>() {

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    override val contentLayoutResId = R.layout.quest_localizedname
    private val binding by contentViewBinding(QuestLocalizednameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoName() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
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

    override fun getLocalizedNameSuggestions(): List<List<LocalizedName>> =
        nameSuggestionsSource.getNames(
            // bus stops are usually not that large, we can just take the center for the dist check
            points = listOf(geometry.center),
            maxDistance = 250.0,
            filter = busStopsWithNamesFilter
        )

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(BusStopName(names))
    }

    private fun confirmNoName() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoBusStopName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
