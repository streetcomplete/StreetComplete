package de.westnordost.streetcomplete.quests.opening_hours

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import java.util.ArrayList

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.opening_hours.adapter.AddOpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer


import android.view.Menu.NONE
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.ktx.toObject
import kotlinx.android.synthetic.main.quest_opening_hours.*

class AddOpeningHoursForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_opening_hours

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_openingHours_no_sign) { confirmNoSign() },
        OtherAnswer(R.string.quest_openingHours_answer_no_regular_opening_hours) { showInputCommentDialog() },
        OtherAnswer(R.string.quest_openingHours_answer_247) { showConfirm24_7Dialog() },
        OtherAnswer(R.string.quest_openingHours_answer_seasonal_opening_hours) { openingHoursAdapter.changeToMonthsMode() }
    )

    private lateinit var openingHoursAdapter: AddOpeningHoursAdapter

    @Inject internal lateinit var serializer: Serializer

    private val openingHoursString get() =
        openingHoursAdapter.createOpeningMonths().joinToString(";")

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAlsoAddingMonths = savedInstanceState?.getBoolean(IS_ADD_MONTHS_MODE) == true
        val viewData = loadOpeningHoursData(savedInstanceState)

        openingHoursAdapter = AddOpeningHoursAdapter(viewData, activity!!, countryInfo)
        openingHoursAdapter.isDisplayMonths = isAlsoAddingMonths
        openingHoursAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openingHoursList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        openingHoursList.adapter = openingHoursAdapter
        openingHoursList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        addTimesButton.setOnClickListener { this.onClickAddButton(it) }
    }

    private fun loadOpeningHoursData(savedInstanceState: Bundle?): List<OpeningMonthsRow> =
        if (savedInstanceState != null) {
            serializer.toObject<ArrayList<OpeningMonthsRow>>(savedInstanceState.getByteArray(OPENING_HOURS_DATA))
        } else {
            listOf(OpeningMonthsRow())
        }

    private fun onClickAddButton(v: View) {
        if (!openingHoursAdapter.isDisplayMonths) {
            openingHoursAdapter.addNewWeekdays()
        } else {
            val popup = PopupMenu(activity!!, v)
            popup.menu.add(NONE, 0, NONE, R.string.quest_openingHours_add_weekdays)
            popup.menu.add(NONE, 1, NONE, R.string.quest_openingHours_add_months)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    0 -> openingHoursAdapter.addNewWeekdays()
                    1 -> openingHoursAdapter.addNewMonths()
                }
                true
            }
            popup.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedTimes = serializer.toBytes(ArrayList(openingHoursAdapter.monthsRows))
        outState.putByteArray(OPENING_HOURS_DATA, serializedTimes)
        outState.putBoolean(IS_ADD_MONTHS_MODE, openingHoursAdapter.isDisplayMonths)
    }

    override fun onClickOk() {
        val answer = Bundle()
        answer.putString(OPENING_HOURS, openingHoursString)
        applyAnswer(answer)
    }

    private fun showInputCommentDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.quest_opening_hours_comment, null)
        val commentInput = view.findViewById<EditText>(R.id.commentInput)

        AlertDialog.Builder(context!!)
            .setTitle(R.string.quest_openingHours_comment_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val txt = commentInput.text.toString().replace("\"","").trim()
                if (txt.isEmpty()) {
                    AlertDialog.Builder(context!!)
                        .setMessage(R.string.quest_openingHours_emptyAnswer)
                        .setPositiveButton(R.string.ok, null)
                        .show()
                } else {
                    val answer = Bundle()
                    answer.putString(OPENING_HOURS, "\"" + txt + "\"")
                    applyAnswer(answer)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showConfirm24_7Dialog() {
        AlertDialog.Builder(activity!!)
            .setMessage(R.string.quest_openingHours_24_7_confirmation)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                val answer = Bundle()
                answer.putString(OPENING_HOURS, "24/7")
                applyAnswer(answer)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun confirmNoSign() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                val data = Bundle()
                data.putBoolean(NO_SIGN, true)
                applyAnswer(data)
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = openingHoursString.isNotEmpty()

    companion object {
        const val OPENING_HOURS = "opening_hours"
        const val NO_SIGN = "no_sign"

        private const val OPENING_HOURS_DATA = "oh_data"
        private const val IS_ADD_MONTHS_MODE = "oh_add_months"
    }
}
