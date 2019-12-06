package de.westnordost.streetcomplete.quests.opening_hours


import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.noButton
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.yesButton
import kotlinx.android.synthetic.main.quest_opening_hours.*
import kotlinx.android.synthetic.main.quest_opening_hours_resurvey.*
import java.util.*

class ResurveyOpeningHoursForm : OpeningHoursForm() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override val contentLayoutResId = R.layout.quest_opening_hours_resurvey
    private var noActionMade = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isAlsoAddingMonths = savedInstanceState?.getBoolean(IS_ADD_MONTHS_MODE) == true
        val viewData = loadOpeningHoursData(savedInstanceState)
        if (viewData.isNotEmpty() && savedInstanceState == null) {
            if (viewData[0].months.start != 0 || viewData[0].months.end != OpeningMonthsRow.MAX_MONTH_INDEX) {
                // loading opening hours state from opening_hours tag
                // it is in the month mode from a start
                isAlsoAddingMonths = true
            }
            openingHoursAdapter = AddOpeningHoursAdapter(viewData, activity!!, countryInfo)
            openingHoursAdapter.isDisplayMonths = isAlsoAddingMonths
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTimesButton.visibility = View.GONE
        yesButton.setOnClickListener { onClickOk() }
        noButton.setOnClickListener {
            okButton.popIn()
            initialButtons.visibility = View.GONE
            addTimesButton.visibility = View.VISIBLE
            noActionMade = false
        }
        okButton.popOut()
    }

    override fun loadOpeningHoursData(savedInstanceState: Bundle?): List<OpeningMonthsRow> =
            if (savedInstanceState != null) {
                serializer.toObject<ArrayList<OpeningMonthsRow>>(savedInstanceState.getByteArray(OPENING_HOURS_DATA)!!)
            } else {
                osmElement!!.tags["opening_hours"]?.let { OpeningHoursTagParser.parse(it) }!!
            }

    override fun onClickOk() {
        val answer = RegularOpeningHours(openingHoursAdapter.createOpeningMonths())
        if (osmElement!!.tags["opening_hours"] == answer.times.joinToString(";")) {
            applyAnswer(UnmodifiedOpeningHours)
        } else {
            applyAnswer(answer)
        }
    }

    override fun isFormComplete(): Boolean {
        if(noActionMade) {
            return false
        }
        return super.isFormComplete()
    }
}
