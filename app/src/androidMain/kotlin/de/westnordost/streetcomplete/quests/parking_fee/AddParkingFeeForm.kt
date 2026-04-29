package de.westnordost.streetcomplete.quests.parking_fee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.osm.maxstay.MaxStay
import de.westnordost.streetcomplete.osm.maxstay.MaxStayInput
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestrictionInput
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddParkingFeeForm : AbstractOsmQuestForm<ParkingFeeAnswer>() {

    @Composable
    override fun Content() {
        var answer by rememberSerializable { mutableStateOf<ParkingFeeAnswer?>(null) }

        QuestForm(
            answers =
                // usually just Yes / No, but user can choose to input a more complex situation
                // in a form through the other answers menu
                if (answer == null) {
                    Answers(
                        Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                            applyAnswer(ParkingFee(Fee.No))
                        },
                        Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                            applyAnswer(ParkingFee(Fee.Yes()))
                        }
                    )
                } else {
                    Confirm(
                        isComplete = answer?.isComplete() == true,
                        hasChanges = answer != null,
                        onClick = { answer?.let { applyAnswer(it) } }
                    )
                },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_fee_answer_hours)) {
                    answer = ParkingFee(Fee.Yes(TimeRestriction(
                        HierarchicOpeningHours(),TimeRestriction.Mode.ONLY_AT_HOURS
                    )))
                },
                Answer(stringResource(Res.string.quest_fee_answer_no_but_maxstay)) {
                    answer = ParkingFeeAnswer.NoFeeButMaxStay(MaxStay(null, null))
                },
            ),
            content = when (val answer2 = answer) {
                is ParkingFeeAnswer.NoFeeButMaxStay -> { {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(Res.string.quest_fee_answer_no_but_maxstay))
                        MaxStayInput(
                            maxStay = answer2.maxstay,
                            onChange = { answer = ParkingFeeAnswer.NoFeeButMaxStay(it) },
                            countryInfo = countryInfo,
                        )
                    }
                } }
                is ParkingFee -> { {
                    if (answer2.fee is Fee.Yes) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(stringResource(Res.string.quest_fee_answer_yes_but))
                            TimeRestrictionInput(
                                timeRestriction = answer2.fee.timeRestriction,
                                onChange = { answer = ParkingFee(Fee.Yes(it)) },
                                countryInfo = countryInfo,
                                allowSelectNoRestriction = false,
                            )
                        }
                    }
                } }
                else -> null
            }
        )
    }
}
