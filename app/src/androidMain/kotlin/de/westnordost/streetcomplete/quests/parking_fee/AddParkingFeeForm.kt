package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.osm.maxstay.MaxStay
import de.westnordost.streetcomplete.osm.maxstay.MaxStayInput
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestrictionInput
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
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
        if (answer.value == null) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(ParkingFee(Fee.No)) },
                AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(ParkingFee(Fee.Yes())) }
            )
        } else {
            emptyList()
        }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_fee_answer_hours) {
            answer.value = ParkingFee(Fee.Yes(TimeRestriction(
                HierarchicOpeningHours(),TimeRestriction.Mode.ONLY_AT_HOURS
            )))
        },
        AnswerItem(R.string.quest_fee_answer_no_but_maxstay) {
            answer.value = ParkingFeeAnswer.NoFeeButMaxStay(MaxStay(null, null))
        },
    )

    private val answer: MutableState<ParkingFeeAnswer?> = mutableStateOf(null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapshotFlow { answer.value }
            .onEach {
                updateButtonPanel()
                checkIsFormComplete()
            }
            .launchIn(lifecycleScope)

        binding.composeViewBase.content { Surface {
            when (val answer2 = answer.value) {
                is ParkingFeeAnswer.NoFeeButMaxStay -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(Res.string.quest_fee_answer_no_but_maxstay))
                        MaxStayInput(
                            maxStay = answer2.maxstay,
                            onChange = { answer.value = ParkingFeeAnswer.NoFeeButMaxStay(it) },
                            countryInfo = countryInfo,
                        )
                    }
                }
                is ParkingFee -> {
                    if (answer2.fee is Fee.Yes) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(stringResource(Res.string.quest_fee_answer_yes_but))
                            TimeRestrictionInput(
                                timeRestriction = answer2.fee.timeRestriction,
                                onChange = { answer.value = ParkingFee(Fee.Yes(it)) },
                                countryInfo = countryInfo,
                                allowSelectNoRestriction = false,
                            )
                        }
                    }
                }
                else -> {
                    // nothing. Note that this still leads to the display of an empty view (padding
                    // plus horizontal bar), this might be solved when the form is migrated to
                    // compose completely
                }
            }
        } }
    }

    override fun onClickOk() {
        answer.value?.let { applyAnswer(it) }

    }

    override fun isRejectingClose(): Boolean = answer.value != null

    override fun isFormComplete() = answer.value?.isComplete() == true
}
