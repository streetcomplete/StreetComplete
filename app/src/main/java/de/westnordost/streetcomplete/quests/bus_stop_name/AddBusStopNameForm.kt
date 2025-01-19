package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestLocalizednameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.NameSuggestionsSource
import org.koin.android.ext.android.inject

class AddBusStopNameForm : AAddLocalizedNameForm<BusStopNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_localizedname
    private val binding by contentViewBinding(QuestLocalizednameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoName() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )
    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    override fun getLocalizedNameSuggestions(): List<List<LocalizedName>> {
        val elementFilter = """
            nodes, ways with
            (
              public_transport = platform and bus = yes
              or (highway = bus_stop and public_transport != stop_position)
              or railway = halt
              or railway = station
              or railway = tram_stop
            )
            and name
        """

        return nameSuggestionsSource.getNames(
            listOf(geometry.center),
            MAX_DIST_FOR_BUS_STOP_NAME_SUGGESTION,
            elementFilter
        )

    }
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

    companion object {
        const val MAX_DIST_FOR_BUS_STOP_NAME_SUGGESTION = 250.0 // m
    }
}
