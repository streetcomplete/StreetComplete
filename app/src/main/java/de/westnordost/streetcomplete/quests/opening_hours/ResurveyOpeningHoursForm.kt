package de.westnordost.streetcomplete.quests.opening_hours


import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu.NONE
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import kotlinx.android.synthetic.main.quest_opening_hours.*
import java.util.*
import javax.inject.Inject

class ResurveyOpeningHoursForm : OpeningHoursForm() {
    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isAlsoAddingMonths = savedInstanceState?.getBoolean(IS_ADD_MONTHS_MODE) == true
        val viewData = loadOpeningHoursData(savedInstanceState)
        if (viewData.size >= 1 && savedInstanceState == null) {
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
