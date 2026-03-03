package de.westnordost.streetcomplete.quests.max_speed

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.max_speed.MaxSpeedSign.Type.*
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WHERE_SLOW_ZONE_IS_LIKELY
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_noSign_confirmation
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_noSign_info_zone
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.stringResource

class AddMaxSpeedForm : AbstractOsmQuestForm<MaxSpeedAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var maxSpeedAnswer = mutableStateOf<MaxSpeedAnswer?>(null)

    override val otherAnswers: List<AnswerItem> get() = buildList {
        if (countryInfo.hasAdvisorySpeedLimitSign) {
            add(AnswerItem(R.string.quest_maxspeed_answer_advisory_speed_limit) {
                maxSpeedAnswer.value = MaxSpeedSign(Speed(null, countryInfo.speedUnits.first()), ADVISORY)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            MaxSpeedForm(
                countryInfo = countryInfo,
                highwayValue = element.tags["highway"]!!,
                answer = maxSpeedAnswer.value,
                onAnswer = {
                    maxSpeedAnswer.value = it
                    checkIsFormComplete()
                },
                initialZoneSpeedValue = LAST_INPUT_SLOW_ZONE,
            )
        } }
    }

    override fun onClickOk() {
        if (maxSpeedAnswer.value is MaxSpeedAnswer.NoSignWithRoadType) {
            if (countryInfo.hasSlowZone && element.tags["highway"] in ROADS_WHERE_SLOW_ZONE_IS_LIKELY) {
                confirmNoSignSlowZone { applySpeedLimitFormAnswer() }
            } else {
                confirmNoSign { applySpeedLimitFormAnswer() }
            }
        } else if (userSelectedUnusualSpeed()) {
            confirmUnusualInput { applySpeedLimitFormAnswer() }
        } else {
            applySpeedLimitFormAnswer()
        }
    }

    override fun isFormComplete() = maxSpeedAnswer.value?.isComplete() == true

    private fun userSelectedUnusualSpeed(): Boolean {
        val maxSpeedSign = maxSpeedAnswer.value as? MaxSpeedSign ?: return false
        val isDividableByFive =  (maxSpeedSign.speed.value ?: 0) % 5 == 0
        val kmh = maxSpeedSign.speed.toKilometersPerHour()
        return when (maxSpeedSign.type) {
            NORMAL, ADVISORY ->
                kmh > 140 || kmh > 20 && !isDividableByFive || kmh < 5
            ZONE ->
                kmh > 40 || kmh > 20 && !isDividableByFive || kmh < 5
        }
    }

    private fun confirmUnusualInput(onConfirmed: () -> Unit) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_maxspeed_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun applySpeedLimitFormAnswer() {
        val maxSpeedSign = maxSpeedAnswer.value as? MaxSpeedSign
        if (maxSpeedSign?.type == ZONE) {
            LAST_INPUT_SLOW_ZONE = maxSpeedSign.speed.value
        }
        applyAnswer(maxSpeedAnswer.value!!)
    }

    /* ----------------------------------------- No sign ---------------------------------------- */

    private fun confirmNoSign(onConfirmed: () -> Unit) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
            .setMessage(R.string.quest_maxspeed_answer_noSign_confirmation)
            .setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive) { _, _ -> onConfirmed() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }

    private fun confirmNoSignSlowZone(onConfirmed: () -> Unit) {
        val view = ComposeView(requireContext())
        view.content {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                ProvideTextStyle(MaterialTheme.typography.body2) {
                    Text(stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation))
                    Text(stringResource(Res.string.quest_maxspeed_answer_noSign_info_zone))
                    MaxSpeedZoneSign(countryInfo = countryInfo) {
                        Text("××", style = MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
                .setView(view)
                .setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    companion object {
        private var LAST_INPUT_SLOW_ZONE: Int? = null
    }
}
