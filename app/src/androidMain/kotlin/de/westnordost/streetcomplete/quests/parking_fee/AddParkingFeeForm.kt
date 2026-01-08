package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.duration.DurationInput
import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.FEE_AT_HOURS
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.FEE_YES_NO
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm.Mode.MAX_STAY
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_fee_answer_no_but_maxstay
import de.westnordost.streetcomplete.resources.quest_fee_answer_yes_but
import de.westnordost.streetcomplete.ui.util.content
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.stringResource

class AddParkingFeeForm : AbstractOsmQuestForm<ParkingFeeAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val buttonPanelAnswers get() =
        if (mode.value == FEE_YES_NO) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(ParkingFeeAnswer(Fee.No)) },
                AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(ParkingFeeAnswer(Fee.Yes)) }
            )
        } else {
            emptyList()
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_fee_answer_hours) { mode.value = FEE_AT_HOURS },
        AnswerItem(R.string.quest_fee_answer_no_but_maxstay) { mode.value = MAX_STAY },
    )

    private enum class Mode { FEE_YES_NO, FEE_AT_HOURS, MAX_STAY }
    private var mode: MutableState<Mode> = mutableStateOf(FEE_YES_NO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { mode.value }
            .onEach {
                updateButtonPanel()
                checkIsFormComplete()
            }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content { Surface {
            if (mode.value == FEE_AT_HOURS) {
                Column {
                    Text(stringResource(Res.string.quest_fee_answer_yes_but))
                    TimeRestrictionInput(

                        countryInfo = countryInfo,
                        locale = countryInfo.userPreferredLocale,
                        userLocale = Locale.current,
                        showAtAnyTime = false,
                    )
                }
            } else if (mode.value == MAX_STAY) {
                Column {
                    Text(stringResource(Res.string.quest_fee_answer_no_but_maxstay))
                    DurationInput(

                    )
                    TimeRestrictionInput(

                        countryInfo = countryInfo,
                        locale = countryInfo.userPreferredLocale,
                        userLocale = Locale.current,
                        showAtAnyTime = true,
                    )
                }
            }
        } }
    }

    override fun onClickOk() {
        /*
        when (mode) {
            FEE_AT_HOURS -> {
                val hours = feeAtHoursSelect!!.times.toOpeningHours()
                val fee = when (feeAtHoursSelect!!.timeRestriction) {
                    AT_ANY_TIME -> Fee.Yes
                    ONLY_AT_HOURS -> Fee.During(hours)
                    EXCEPT_AT_HOURS -> Fee.ExceptDuring(hours)
                }
                applyAnswer(ParkingFeeAnswer(fee))
            }
            MAX_STAY -> {
                val duration = MaxStay.Duration(
                    maxstayDurationInput!!.durationValue,
                    when (maxstayDurationInput!!.durationUnit) {
                        DurationUnit.MINUTES -> MaxStay.Unit.MINUTES
                        DurationUnit.HOURS -> MaxStay.Unit.HOURS
                        DurationUnit.DAYS -> MaxStay.Unit.DAYS
                    }
                )
                val hours = maxstayAtHoursSelect!!.times.toOpeningHours()
                val maxstay = when (maxstayAtHoursSelect!!.timeRestriction) {
                    AT_ANY_TIME -> duration
                    ONLY_AT_HOURS -> MaxStay.During(duration, hours)
                    EXCEPT_AT_HOURS -> MaxStay.ExceptDuring(duration, hours)
                }
                applyAnswer(ParkingFeeAnswer(Fee.No, maxstay))
            }
            else -> {}
        }

         */
    }

    override fun isRejectingClose() = when (mode) {
        /*
        FEE_AT_HOURS -> feeAtHoursSelect!!.isComplete
        MAX_STAY -> maxstayAtHoursSelect!!.isComplete || maxstayDurationInput!!.durationValue > 0.0

         */
        else -> false
    }

    override fun isFormComplete() = when (mode) {
        /*
        FEE_AT_HOURS -> feeAtHoursSelect!!.isComplete
        MAX_STAY -> maxstayAtHoursSelect!!.isComplete && maxstayDurationInput!!.durationValue > 0.0

         */
        else -> false
    }

}
