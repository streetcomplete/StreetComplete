package de.westnordost.streetcomplete.quests.road_name

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestRoadnameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.koin.android.ext.android.inject
import java.lang.IllegalStateException
import java.util.LinkedList
import java.util.Locale

class AddRoadNameForm : AAddLocalizedNameForm<RoadNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_roadname
    private val binding by contentViewBinding(QuestRoadnameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val adapterRowLayoutResId = R.layout.quest_roadname_row

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_name_answer_noName) { selectNoStreetNameReason() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )

    private val abbrByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    override fun getAbbreviationsByLocale(): AbbreviationsByLocale = abbrByLocale

    override fun getLocalizedNameSuggestions(): List<List<LocalizedName>> {
        val polyline = when (val geom = geometry) {
            is ElementPolylinesGeometry -> geom.polylines.first()
            is ElementPolygonsGeometry -> geom.polygons.first()
            is ElementPointGeometry -> listOf(geom.center)
        }
        return roadNameSuggestionsSource.getNames(
            listOf(polyline.first(), polyline.last()),
            MAX_DIST_FOR_ROAD_NAME_SUGGESTION
        )
    }

    override fun onClickOk(names: List<LocalizedName>) {
        val possibleAbbreviations = LinkedList<String>()
        for ((languageTag, name) in adapter?.names.orEmpty()) {
            val locale = if (languageTag.isEmpty()) countryInfo.locale else Locale.forLanguageTag(languageTag)
            val abbr = abbrByLocale.get(locale)
            val containsLocalizedAbbreviations = abbr?.containsAbbreviations(name) == true

            if (name.contains(".") || containsLocalizedAbbreviations) {
                possibleAbbreviations.add(name)
            }
        }

        confirmPossibleAbbreviationsIfAny(possibleAbbreviations) {
            val points = when (val g = geometry) {
                is ElementPolylinesGeometry -> g.polylines.first()
                is ElementPolygonsGeometry -> g.polygons.first()
                is ElementPointGeometry -> listOf(g.center)
            }
            applyAnswer(RoadName(names, element.id, points))
        }
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
                        linkRoad    -> RoadIsLinkRoad
                        serviceRoad -> RoadIsServiceRoad
                        trackRoad   -> RoadIsTrack
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
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoRoadName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    companion object {
        const val MAX_DIST_FOR_ROAD_NAME_SUGGESTION = 30.0 // m
    }
}
