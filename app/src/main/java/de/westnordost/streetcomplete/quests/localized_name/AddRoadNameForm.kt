package de.westnordost.streetcomplete.quests.localized_name

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import java.util.ArrayList
import java.util.LinkedList
import java.util.Locale

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import java.lang.IllegalStateException


class AddRoadNameForm : AddLocalizedNameForm() {

    @Inject internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        Injector.instance.applicationComponent.inject(this)

        val contentView = setContentView(R.layout.quest_localizedname)

        addOtherAnswer(R.string.quest_name_answer_noName) { selectNoStreetNameReason() }
        addOtherAnswer(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }

        initLocalizedNameAdapter(contentView, savedInstanceState)

        return view
    }

    override fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: Button): AddLocalizedNameAdapter {
        return AddLocalizedNameAdapter(
            data, activity!!, getPossibleStreetsignLanguages(),
            abbreviationsByLocale, getRoadNameSuggestions(), addLanguageButton
        )
    }

    private fun getRoadNameSuggestions(): List<MutableMap<String, String>> {
        val points = elementGeometry?.polylines?.getOrNull(0) ?: return listOf()
        val onlyFirstAndLast = listOf(points[0], points[points.size - 1])

        return roadNameSuggestionsDao.getNames(
            onlyFirstAndLast, AddRoadName.MAX_DIST_FOR_ROAD_NAME_SUGGESTION)
    }

    override fun onClickOk() {
        val possibleAbbreviations = LinkedList<String>()
        for ((languageCode, name) in adapter.localizedNames) {
            if (name.trim().isEmpty()) {
                Toast.makeText(activity, R.string.quest_generic_error_a_field_empty, Toast.LENGTH_LONG).show()
                return
            }

            val abbr = abbreviationsByLocale.get(Locale(languageCode))
            val containsAbbreviations = abbr?.containsAbbreviations(name) == true

            if (name.contains(".") || containsAbbreviations) {
                possibleAbbreviations.add(name)
            }
        }

        confirmPossibleAbbreviationsIfAny(possibleAbbreviations) { applyNameAnswer() }
    }

    override fun applyNameAnswer() {
        val bundle = createAnswer()
        bundle.putLong(WAY_ID, osmElement.id)
        bundle.putSerializable(WAY_GEOMETRY, elementGeometry)
        applyAnswer(bundle)
    }

    private fun selectNoStreetNameReason() {
        val linkRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_link)
        val serviceRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_service2)
        val trackRoad = resources.getString(R.string.quest_streetName_answer_noProperStreet_track2)
        val noName = resources.getString(R.string.quest_streetName_answer_noName_noname)
        val leaveNote = resources.getString(R.string.quest_streetName_answer_noProperStreet_leaveNote)

        val highwayValue = osmElement.tags["highway"]
        val mayBeLink = highwayValue?.matches("primary|secondary|tertiary".toRegex()) == true

        val answers = ArrayList<String>(5)
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
                        if(it >= 0 && it < answers.size) onAnswer(it)
                    }
                }
            }

            private fun onAnswer(selection: Int) {
                val answer = answers[selection]
                when (answer) {
                    leaveNote -> onClickCantSay()
                    noName    -> confirmNoStreetName()
                    else      -> {
                        val type = when(answer) {
                            linkRoad    -> IS_LINK
                            serviceRoad -> IS_SERVICE
                            trackRoad   -> IS_TRACK
                            else        -> throw IllegalStateException()
                        }
                        val data = Bundle()
                        data.putInt(NO_PROPER_ROAD, type)
                        applyAnswer(data)
                    }
                }
            }
        }

        val dlg = AlertDialog.Builder(activity!!)
            .setSingleChoiceItems(answers.toTypedArray(), -1, onSelect)
            .setTitle(R.string.quest_streetName_answer_noName_question)
            .setPositiveButton(android.R.string.ok, onSelect)
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        dlg.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
    }

    private fun confirmNoStreetName() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ ->
                val data = Bundle()
                data.putBoolean(AddLocalizedNameForm.NO_NAME, true)
                applyAnswer(data)
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    companion object {

        val NO_PROPER_ROAD = "no_proper_road"
        val WAY_ID = "way_id"
        val WAY_GEOMETRY = "way_geometry"

        val IS_SERVICE = 1
        val IS_LINK = 2
        val IS_TRACK = 3
    }
}
