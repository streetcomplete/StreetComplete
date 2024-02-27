package de.westnordost.streetcomplete.quests.opening_hours

import android.os.Bundle
import android.view.Menu.NONE
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestOpeningHoursBinding
import de.westnordost.streetcomplete.databinding.QuestOpeningHoursCommentBinding
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRows
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningHoursAdapter
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddOpeningHoursForm : AbstractOsmQuestForm<OpeningHoursAnswer>() {

    override val contentLayoutResId = R.layout.quest_opening_hours
    private val binding by contentViewBinding(QuestOpeningHoursBinding::bind)

    override val buttonPanelAnswers get() =
        if (isDisplayingPreviousOpeningHours) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { setAsResurvey(false) },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(RegularOpeningHours(
                        element.tags["opening_hours"]!!.toOpeningHours(lenient = true)
                    ))
                }
            )
        } else {
            emptyList()
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_openingHours_no_sign) { confirmNoSign() },
        AnswerItem(R.string.quest_openingHours_answer_no_regular_opening_hours) { showInputCommentDialog() },
        AnswerItem(R.string.quest_openingHours_answer_247) { showConfirm24_7Dialog() },
        AnswerItem(R.string.quest_openingHours_answer_seasonal_opening_hours) {
            setAsResurvey(false)
            openingHoursAdapter.changeToMonthsMode()
        }
    )

    private lateinit var openingHoursAdapter: OpeningHoursAdapter

    private var isDisplayingPreviousOpeningHours: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openingHoursAdapter = OpeningHoursAdapter(requireContext())
        openingHoursAdapter.firstDayOfWorkweek = countryInfo.firstDayOfWorkweek
        openingHoursAdapter.regularShoppingDays = countryInfo.regularShoppingDays
        openingHoursAdapter.locale = countryInfo.userPreferredLocale
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        openingHoursAdapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })

        binding.openingHoursList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.openingHoursList.adapter = openingHoursAdapter
        binding.openingHoursList.isNestedScrollingEnabled = false
        checkIsFormComplete()

        binding.addTimesButton.setOnClickListener { onClickAddButton(it) }
    }

    private fun onClickAddButton(v: View) {
        val rows = openingHoursAdapter.rows

        val addMonthAvailable = rows.any { it is OpeningMonthsRow }
        val addTimeAvailable = rows.isNotEmpty() && rows.last() is OpeningWeekdaysRow
        val addOffDayAvailable = rows.isNotEmpty() && rows.last() is OpeningWeekdaysRow

        if (addMonthAvailable || addTimeAvailable || addOffDayAvailable) {
            val popup = PopupMenu(requireContext(), v)
            if (addTimeAvailable) popup.menu.add(NONE, 0, NONE, R.string.quest_openingHours_add_hours)
            popup.menu.add(NONE, 1, NONE, R.string.quest_openingHours_add_weekdays)
            if (addOffDayAvailable) popup.menu.add(NONE, 2, NONE, R.string.quest_openingHours_add_off_days)
            if (addMonthAvailable) popup.menu.add(NONE, 3, NONE, R.string.quest_openingHours_add_months)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> openingHoursAdapter.addNewHours()
                    1 -> openingHoursAdapter.addNewWeekdays()
                    2 -> openingHoursAdapter.addNewOffDays()
                    3 -> openingHoursAdapter.addNewMonths()
                }
                true
            }
            popup.show()
        } else {
            openingHoursAdapter.addNewWeekdays()
        }
    }

    private fun initStateFromTags() {
        val oh = element.tags["opening_hours"]
        val rows = oh?.toOpeningHoursOrNull(lenient = true)?.toOpeningHoursRows()
        if (rows != null) {
            openingHoursAdapter.rows = rows.toMutableList()
            setAsResurvey(true)
        } else {
            setAsResurvey(false)
        }
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        openingHoursAdapter.rows = Json.decodeFromString(savedInstanceState.getString(OPENING_HOURS_DATA)!!)
        setAsResurvey(savedInstanceState.getBoolean(IS_DISPLAYING_PREVIOUS_HOURS))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(OPENING_HOURS_DATA, Json.encodeToString(openingHoursAdapter.rows))
        outState.putBoolean(IS_DISPLAYING_PREVIOUS_HOURS, isDisplayingPreviousOpeningHours)
    }

    override fun onClickOk() {
        applyAnswer(RegularOpeningHours(openingHoursAdapter.createOpeningHours()))
    }

    private fun showInputCommentDialog() {
        val dialogBinding = QuestOpeningHoursCommentBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_openingHours_comment_title)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val txt = dialogBinding.commentInput.text.toString().replace("\"", "").trim()
                if (txt.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.quest_openingHours_emptyAnswer)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                } else {
                    applyAnswer(DescribeOpeningHours(txt))
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setAsResurvey(resurvey: Boolean) {
        openingHoursAdapter.isEnabled = !resurvey
        isDisplayingPreviousOpeningHours = resurvey
        binding.addTimesButton.isGone = resurvey
        updateButtonPanel()
    }

    private fun showConfirm24_7Dialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_openingHours_24_7_confirmation)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ ->
                applyAnswer(AlwaysOpen)
            }
            .setNegativeButton(R.string.quest_generic_hasFeature_no, null)
            .show()
    }

    private fun confirmNoSign() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(NoOpeningHoursSign) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = openingHoursAdapter.rows.isNotEmpty() && !isDisplayingPreviousOpeningHours

    companion object {
        private const val OPENING_HOURS_DATA = "oh_data"
        private const val IS_DISPLAYING_PREVIOUS_HOURS = "oh_is_displaying_previous_hours"
    }
}
