package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestFeeHoursBinding
import de.westnordost.streetcomplete.databinding.QuestMaxstayBinding
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.*
import de.westnordost.streetcomplete.view.DurationUnit
import de.westnordost.streetcomplete.view.TimeRestriction.AT_ANY_TIME
import de.westnordost.streetcomplete.view.TimeRestriction.EXCEPT_AT_HOURS
import de.westnordost.streetcomplete.view.TimeRestriction.ONLY_AT_HOURS

class AddParkingFeeForm : AbstractQuestFormAnswerFragment<FeeAndMaxStay>() {

    private var binding: ViewBinding? = null

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
            field = value
            binding = when (mode) {
                FEE_YES_NO -> null
                FEE_AT_HOURS -> QuestFeeHoursBinding.bind(setContentView(R.layout.quest_fee_hours))
                MAX_STAY -> QuestMaxstayBinding.bind(setContentView(R.layout.quest_maxstay))
            }
            onContentViewBound()
            updateButtonPanel()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mode = savedInstanceState?.getString(MODE)?.let { valueOf(it) } ?: FEE_YES_NO
        checkIsFormComplete()
    }

    private fun onContentViewBound() {
        val binding = binding
        if (binding is QuestFeeHoursBinding) {
            binding.timesView.firstDayOfWorkweek = countryInfo.firstDayOfWorkweek
            binding.timesView.regularShoppingDays = countryInfo.regularShoppingDays
            binding.timesView.selectableTimeRestrictions = listOf(ONLY_AT_HOURS, EXCEPT_AT_HOURS)
            binding.timesView.onInputChanged = { checkIsFormComplete() }
        } else if (binding is QuestMaxstayBinding) {
            binding.timesView.firstDayOfWorkweek = countryInfo.firstDayOfWorkweek
            binding.timesView.regularShoppingDays = countryInfo.regularShoppingDays
            binding.timesView.onInputChanged = { checkIsFormComplete() }
            binding.durationInput.onInputChanged = { checkIsFormComplete() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onClickOk() {
        val binding = binding
        if (binding is QuestFeeHoursBinding) {
            val hours = binding.timesView.hours.toOpeningHoursRules()
            val fee = when (binding.timesView.timeRestriction) {
                AT_ANY_TIME -> HasFee
                ONLY_AT_HOURS -> HasFeeAtHours(hours)
                EXCEPT_AT_HOURS -> HasFeeExceptAtHours(hours)
            }
            applyAnswer(FeeAndMaxStay(fee))
        } else if (binding is QuestMaxstayBinding) {
            val duration = MaxstayDuration(
                binding.durationInput.durationValue,
                when (binding.durationInput.durationUnit) {
                    DurationUnit.MINUTES -> Maxstay.Unit.MINUTES
                    DurationUnit.HOURS -> Maxstay.Unit.HOURS
                    DurationUnit.DAYS -> Maxstay.Unit.DAYS
                }
            )
            val hours = binding.timesView.hours.toOpeningHoursRules()
            val maxstay = when (binding.timesView.timeRestriction) {
                AT_ANY_TIME -> duration
                ONLY_AT_HOURS -> MaxstayAtHours(duration, hours)
                EXCEPT_AT_HOURS -> MaxstayExceptAtHours(duration, hours)
            }
            applyAnswer(FeeAndMaxStay(HasNoFee, maxstay))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MODE, mode.name)
    }

    override fun isRejectingClose() = when (val binding = binding) {
        is QuestFeeHoursBinding -> binding.timesView.hours.isNotEmpty()
        is QuestMaxstayBinding ->  binding.timesView.isComplete || binding.durationInput.durationValue > 0.0
        else -> false
    }

    override fun isFormComplete() = when (val binding = binding) {
        is QuestFeeHoursBinding -> binding.timesView.hours.isNotEmpty()
        is QuestMaxstayBinding -> binding.timesView.isComplete && binding.durationInput.durationValue > 0.0
        else -> false
    }

    companion object {
        private const val MODE = "mode"
    }

    private enum class Mode { FEE_YES_NO, FEE_AT_HOURS, MAX_STAY }
}
