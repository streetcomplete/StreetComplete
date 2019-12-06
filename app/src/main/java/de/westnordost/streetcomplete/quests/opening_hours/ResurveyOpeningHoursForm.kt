package de.westnordost.streetcomplete.quests.opening_hours


import android.os.Bundle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import java.util.*

class ResurveyOpeningHoursForm : OpeningHoursForm() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override val contentLayoutResId = R.layout.quest_opening_hours_resurvey

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
}
