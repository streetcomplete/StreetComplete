package de.westnordost.streetcomplete.quests.road_name

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo
import org.koin.android.ext.android.inject

class AddRoadNameForm : AAddLocalizedNameForm<RoadNameAnswer>() {

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_name_answer_noName) { selectNoStreetNameReason() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo(requireContext()) }
    )

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        nameSuggestionsSource.getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.let { localizedNames.value = it }

        return true
    }

    override fun showAbbreviationsHint(): Boolean = true

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(RoadName(names))
    }

    private fun selectNoStreetNameReason() {
        val linkRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_link)
        val serviceRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_service2)
        val trackRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_track2)
        val noName = resources.getString(R.string.quest_streetName_answer_noName_noname)
        val leaveNote = resources.getString(R.string.quest_streetName_answer_noProperStreet_leaveNote)

        val highwayValue = element.tags["highway"]
        val mayBeLink = highwayValue?.matches("primary|secondary|tertiary".toRegex()) == true

        val answers = mutableListOf<String>()
        if (mayBeLink) answers.add(linkRoad)
        answers.add(serviceRoad)
        answers.add(trackRoad)
        answers.add(leaveNote)
        answers.add(noName)

        val onSelect = object : DialogInterface.OnClickListener {
            var selection: Int? = null

            override fun onClick(dialog: DialogInterface, which: Int) {
                if (which >= 0) {
                    selection = which
                    (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = true
                } else if (which == DialogInterface.BUTTON_POSITIVE) {
                    selection?.let {
                        if (it >= 0 && it < answers.size) onAnswer(it)
                    }
                }
            }

            private fun onAnswer(selection: Int) {
                val answer = answers[selection]
                when (answer) {
                    leaveNote -> composeNote()
                    noName    -> confirmNoStreetName()
                    else      -> applyAnswer(when (answer) {
                        linkRoad    -> RoadNameAnswer.IsLinkRoad
                        serviceRoad -> RoadNameAnswer.IsServiceRoad
                        trackRoad   -> RoadNameAnswer.IsTrack
                        else        -> throw IllegalStateException()
                    })
                }
            }
        }

        val dlg = AlertDialog.Builder(requireContext())
            .setSingleChoiceItems(answers.toTypedArray(), -1, onSelect)
            .setTitle(R.string.quest_streetName_answer_noName_question)
            .setPositiveButton(android.R.string.ok, onSelect)
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        dlg.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
    }

    private fun confirmNoStreetName() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(RoadNameAnswer.NoName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
