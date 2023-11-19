package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFeeHoursBinding
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.barrier_locked.AddBarrierLockedForm.Mode.LOCKED_AT_HOURS
import de.westnordost.streetcomplete.quests.barrier_locked.AddBarrierLockedForm.Mode.LOCKED_YES_NO
import de.westnordost.streetcomplete.view.controller.TimeRestriction.AT_ANY_TIME
import de.westnordost.streetcomplete.view.controller.TimeRestriction.EXCEPT_AT_HOURS
import de.westnordost.streetcomplete.view.controller.TimeRestriction.ONLY_AT_HOURS
import de.westnordost.streetcomplete.view.controller.TimeRestrictionSelectViewController

class AddBarrierLockedForm : AbstractOsmQuestForm<BarrierLockedAnswer>() {

    private var lockedAtHoursSelect: TimeRestrictionSelectViewController? = null

    override val buttonPanelAnswers get() =
        if (mode == LOCKED_YES_NO) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NotLocked) },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(Locked) }
        )
        else emptyList()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_fee_answer_hours) { mode = LOCKED_AT_HOURS },
    )

    private var mode: Mode = LOCKED_YES_NO
        set(value) {
            if (field == value) return
            field = value
            updateContentView()
            updateButtonPanel()
        }

    private fun updateContentView() {
        clearViewControllers()

        if (mode == LOCKED_AT_HOURS) {
            val binding = QuestFeeHoursBinding.bind(setContentView(R.layout.quest_fee_hours))

            lockedAtHoursSelect = TimeRestrictionSelectViewController(
                binding.timeRestrictionSelect.selectAtHours,
                binding.timeRestrictionSelect.openingHoursList,
                binding.timeRestrictionSelect.addTimesButton
            ).also {
                it.firstDayOfWorkweek = countryInfo.firstDayOfWorkweek
                it.regularShoppingDays = countryInfo.regularShoppingDays
                it.locale = countryInfo.userPreferredLocale
                it.onInputChanged = { checkIsFormComplete() }
                // user already answered that it depends on the time, so don't show the "at any time" option
                it.selectableTimeRestrictions = listOf(ONLY_AT_HOURS, EXCEPT_AT_HOURS)
            }
        }
    }

    private fun clearViewControllers() {
        lockedAtHoursSelect = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearViewControllers()
    }

    override fun onClickOk() {
        when (mode) {
            LOCKED_AT_HOURS -> {
                val hours = lockedAtHoursSelect!!.times.toOpeningHoursRules()
                val locked = when (lockedAtHoursSelect!!.timeRestriction) {
                    AT_ANY_TIME -> Locked
                    ONLY_AT_HOURS -> LockedAtHours(hours)
                    EXCEPT_AT_HOURS -> LockedExceptAtHours(hours)
                }
                applyAnswer(locked)
            }
            else -> {}
        }
    }

    override fun isRejectingClose() = when (mode) {
        LOCKED_AT_HOURS -> lockedAtHoursSelect!!.isComplete
        else -> false
    }

    override fun isFormComplete() = when (mode) {
        LOCKED_AT_HOURS -> lockedAtHoursSelect!!.isComplete
        else -> false
    }

    private enum class Mode { LOCKED_YES_NO, LOCKED_AT_HOURS }
}
