package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFeeHoursBinding
import de.westnordost.streetcomplete.databinding.QuestMaxstayBinding
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.FEE_AT_HOURS
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.FEE_YES_NO
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.MAX_STAY
import de.westnordost.streetcomplete.view.controller.DurationInputViewController
import de.westnordost.streetcomplete.view.controller.DurationUnit
import de.westnordost.streetcomplete.view.controller.TimeRestriction.AT_ANY_TIME
import de.westnordost.streetcomplete.view.controller.TimeRestriction.EXCEPT_AT_HOURS
import de.westnordost.streetcomplete.view.controller.TimeRestriction.ONLY_AT_HOURS
import de.westnordost.streetcomplete.view.controller.TimeRestrictionSelectViewController

class AddParkingFeeForm : AbstractOsmQuestForm<FeeAndMaxStay>() {

    private var feeAtHoursSelect: TimeRestrictionSelectViewController? = null

    private var maxstayDurationInput: DurationInputViewController? = null
    private var maxstayAtHoursSelect: TimeRestrictionSelectViewController? = null

    override val buttonPanelAnswers get() =
        if (mode == FEE_YES_NO) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(FeeAndMaxStay(HasNoFee)) },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(FeeAndMaxStay(HasFee)) }
        )
        else emptyList()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_fee_answer_hours) { mode = FEE_AT_HOURS },
        AnswerItem(R.string.quest_fee_answer_no_but_maxstay) { mode = MAX_STAY },
    )

    private var mode: Mode = FEE_YES_NO
        set(value) {
            if (field == value) return
            field = value
            updateContentView()
            updateButtonPanel()
        }

    private fun updateContentView() {
        clearViewControllers()

        if (mode == FEE_AT_HOURS) {
            val binding = QuestFeeHoursBinding.bind(setContentView(R.layout.quest_fee_hours))

            feeAtHoursSelect = TimeRestrictionSelectViewController(
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
        } else if (mode == MAX_STAY) {
            val binding = QuestMaxstayBinding.bind(setContentView(R.layout.quest_maxstay))

            maxstayDurationInput = DurationInputViewController(
                binding.durationInput.unitSelect,
                binding.durationInput.input
            ).also {
                it.onInputChanged = { checkIsFormComplete() }
            }
            maxstayAtHoursSelect = TimeRestrictionSelectViewController(
                binding.timeRestrictionSelect.selectAtHours,
                binding.timeRestrictionSelect.openingHoursList,
                binding.timeRestrictionSelect.addTimesButton
            ).also {
                it.firstDayOfWorkweek = countryInfo.firstDayOfWorkweek
                it.regularShoppingDays = countryInfo.regularShoppingDays
                it.locale = countryInfo.userPreferredLocale
                it.onInputChanged = { checkIsFormComplete() }
            }
        }
    }

    private fun clearViewControllers() {
        feeAtHoursSelect = null
        maxstayAtHoursSelect = null
        maxstayDurationInput = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearViewControllers()
    }

    override fun onClickOk() {
        when (mode) {
            FEE_AT_HOURS -> {
                val hours = feeAtHoursSelect!!.times.toOpeningHoursRules()
                val fee = when (feeAtHoursSelect!!.timeRestriction) {
                    AT_ANY_TIME -> HasFee
                    ONLY_AT_HOURS -> HasFeeAtHours(hours)
                    EXCEPT_AT_HOURS -> HasFeeExceptAtHours(hours)
                }
                applyAnswer(FeeAndMaxStay(fee))
            }
            MAX_STAY -> {
                val duration = MaxstayDuration(
                    maxstayDurationInput!!.durationValue,
                    when (maxstayDurationInput!!.durationUnit) {
                        DurationUnit.MINUTES -> Maxstay.Unit.MINUTES
                        DurationUnit.HOURS -> Maxstay.Unit.HOURS
                        DurationUnit.DAYS -> Maxstay.Unit.DAYS
                    }
                )
                val hours = maxstayAtHoursSelect!!.times.toOpeningHoursRules()
                val maxstay = when (maxstayAtHoursSelect!!.timeRestriction) {
                    AT_ANY_TIME -> duration
                    ONLY_AT_HOURS -> MaxstayAtHours(duration, hours)
                    EXCEPT_AT_HOURS -> MaxstayExceptAtHours(duration, hours)
                }
                applyAnswer(FeeAndMaxStay(HasNoFee, maxstay))
            }
            else -> {}
        }
    }

    override fun isRejectingClose() = when (mode) {
        FEE_AT_HOURS -> feeAtHoursSelect!!.isComplete
        MAX_STAY -> maxstayAtHoursSelect!!.isComplete || maxstayDurationInput!!.durationValue > 0.0
        else -> false
    }

    override fun isFormComplete() = when (mode) {
        FEE_AT_HOURS -> feeAtHoursSelect!!.isComplete
        MAX_STAY -> maxstayAtHoursSelect!!.isComplete && maxstayDurationInput!!.durationValue > 0.0
        else -> false
    }

    private enum class Mode { FEE_YES_NO, FEE_AT_HOURS, MAX_STAY }
}
