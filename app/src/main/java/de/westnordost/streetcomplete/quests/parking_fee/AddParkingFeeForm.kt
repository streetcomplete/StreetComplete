package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter

import java.util.ArrayList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import kotlinx.android.synthetic.main.fragment_quest_answer.*
import kotlinx.android.synthetic.main.quest_buttonpanel_yes_no.*
import kotlinx.android.synthetic.main.quest_fee_hours.*

class AddParkingFeeForm : AbstractQuestFormAnswerFragment() {

    private lateinit var openingHoursAdapter: AddOpeningHoursAdapter
    private var hoursView: View? = null

    @Inject internal lateinit var serializer: Serializer

    private var isDefiningHours: Boolean = false
    set(value) {
        field = value

        hoursView?.visibility = if (value) View.VISIBLE else View.GONE
        noButton?.visibility = if (value) View.GONE else View.VISIBLE
        yesButton?.visibility = if (value) View.GONE else View.VISIBLE
    }
    private var isFeeOnlyAtHours: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        Injector.instance.applicationComponent.inject(this)

        setButtonsView(R.layout.quest_buttonpanel_yes_no)

        okButton.setOnClickListener { onClickOk() }
        yesButton.setOnClickListener { onClickYesNo(true) }
        noButton.setOnClickListener { onClickYesNo(false) }

        addOtherAnswer(R.string.quest_fee_answer_hours) { isDefiningHours = true }

        hoursView = setContentView(R.layout.quest_fee_hours)

        val viewData = loadOpeningHoursData(savedInstanceState)
        openingHoursAdapter = AddOpeningHoursAdapter(viewData, activity, countryInfo)
        openingHoursAdapter.registerAdapterDataObserver( AdapterDataChangedWatcher { checkIsFormComplete() })

        openingHoursList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        openingHoursList.adapter = openingHoursAdapter
        openingHoursList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        addTimesButton.setOnClickListener { openingHoursAdapter.addNewWeekdays() }

        isFeeOnlyAtHours = savedInstanceState?.getBoolean(IS_FEE_ONLY_AT_HOURS, true) ?: true
        isDefiningHours = savedInstanceState?.getBoolean(IS_DEFINING_HOURS) ?: false

        val spinnerItems = listOf(
            getString(R.string.quest_fee_only_at_hours),
            getString(R.string.quest_fee_not_at_hours)
        )
        selectFeeOnlyAtHours.adapter = ArrayAdapter(activity, R.layout.spinner_item_centered, spinnerItems)
        selectFeeOnlyAtHours.setSelection(if (isFeeOnlyAtHours) 0 else 1)
        selectFeeOnlyAtHours.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                isFeeOnlyAtHours = position == 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    override fun onClickOk() {
        val bundle = Bundle()
        bundle.putBoolean(FEE, !isFeeOnlyAtHours)
        val oh = getOpeningHoursString()
        if (!oh.isEmpty()) {
            bundle.putString(FEE_CONDITONAL_HOURS, oh)
        }
        applyAnswer(bundle)
    }

    private fun onClickYesNo(answer: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(FEE, answer)
        applyAnswer(bundle)
    }

    private fun loadOpeningHoursData(savedInstanceState: Bundle?): ArrayList<OpeningMonthsRow> {
        val viewData: ArrayList<OpeningMonthsRow>
        if (savedInstanceState != null) {
            viewData = serializer.toObject(savedInstanceState.getByteArray(OPENING_HOURS_DATA))
        } else {
            viewData = ArrayList()
            viewData.add(OpeningMonthsRow())
        }
        return viewData
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putByteArray(
            OPENING_HOURS_DATA,
            serializer.toBytes(openingHoursAdapter.viewData)
        )
        outState.putBoolean(IS_DEFINING_HOURS, isDefiningHours)
        outState.putBoolean(IS_FEE_ONLY_AT_HOURS, isFeeOnlyAtHours)
    }

    override fun isFormComplete() = !isDefiningHours && !getOpeningHoursString().isEmpty()

    private fun getOpeningHoursString() = openingHoursAdapter.createOpeningMonths().joinToString(";")


    companion object {

        val FEE = "fee"
        val FEE_CONDITONAL_HOURS = "fee_conditional_hours"

        private val OPENING_HOURS_DATA = "oh_data"
        private val IS_FEE_ONLY_AT_HOURS = "oh_fee_only_at"
        private val IS_DEFINING_HOURS = "oh"
    }
}
