package de.westnordost.streetcomplete.quests.opening_hours

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osm_opening_hours.parser.toOpeningHours
import de.westnordost.osm_opening_hours.parser.toOpeningHoursOrNull
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.AdapterDataChangedWatcher

class AddOpeningHoursForm : AbstractOsmQuestForm<OpeningHoursAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

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

    private fun initStateFromTags() {
        val oh = element.tags["opening_hours"]
        val rows = oh?.toOpeningHoursOrNull(lenient = true)?.toHierarchicOpeningHours()
        if (rows != null) {
            openingHoursAdapter.rows = rows.toMutableList()
            setAsResurvey(true)
        } else {
            setAsResurvey(false)
        }
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        setAsResurvey(savedInstanceState.getBoolean(IS_DISPLAYING_PREVIOUS_HOURS))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

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
}
