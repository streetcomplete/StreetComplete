package de.westnordost.streetcomplete.quests.max_speed

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.databinding.QuestMaxspeedNoSignNoSlowZoneConfirmationBinding
import de.westnordost.streetcomplete.osm.maxspeed.COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WHERE_SLOW_ZONE_IS_LIKELY
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.util.content

class AddMaxSpeedForm : AbstractOsmQuestForm<MaxSpeedAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private var maxSpeedAnswer = mutableStateOf<MaxSpeedAnswer?>(null)
    private var initialSelectedMaxSpeedType = mutableStateOf<MaxSpeedType?>(null)

    override val otherAnswers: List<AnswerItem> get() = buildList {
        if (countryInfo.hasAdvisorySpeedLimitSign) {
            add(AnswerItem(R.string.quest_maxspeed_answer_advisory_speed_limit) {
                initialSelectedMaxSpeedType.value = MaxSpeedType.ADVISORY
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            MaxSpeedForm(
                initialSelectedMaxSpeedType = initialSelectedMaxSpeedType.value,
                countryInfo = countryInfo,
                highwayValue = element.tags["highway"]!!,
                maxSpeed = maxSpeedAnswer.value,
                onMaxSpeed = {
                    maxSpeedAnswer.value = it
                    checkIsFormComplete()
                },
            )
        } }
    }

    override fun onClickOk() {
        if (maxSpeedAnswer.value is DefaultMaxSpeed) {
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

    override fun isFormComplete() = maxSpeedAnswer.value != null

    private fun userSelectedUnusualSpeed(): Boolean {
        val speed = maxSpeedAnswer.value?.getSpeedOrNull() ?: return false
        val isDividableByFive = speed.value % 5 == 0
        val kmh = speed.toKilometersPerHour()
        return when (maxSpeedAnswer.value) {
            is AdvisorySpeedSign,
            is MaxSpeedSign -> kmh > 140 || kmh > 20 && !isDividableByFive || kmh < 5
            is MaxSpeedZone -> kmh > 40 || kmh > 20 && !isDividableByFive || kmh < 5
            else -> false
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
        applyAnswer(maxSpeedAnswer.value!!)
    }

    /* ----------------------------------------- No sign ---------------------------------------- */

    private fun confirmNoSign(onConfirmed: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
                .setMessage(R.string.quest_maxspeed_answer_noSign_confirmation)
                .setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    private fun confirmNoSignSlowZone(onConfirmed: () -> Unit) {
        // TODO
        activity?.let {
            val dialogBinding = QuestMaxspeedNoSignNoSlowZoneConfirmationBinding.inflate(layoutInflater)
            //enableAppropriateLabelsForSlowZone(dialogBinding.slowZoneImage)
            dialogBinding.slowZoneImage.removeAllViews()

            AlertDialog.Builder(it)
                .setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    private fun applyNoSignAnswer(roadType: String, lit: Boolean? = null) {
        val cc = countryOrSubdivisionCode.orEmpty()
        val useSubdivisionCode = COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS.any { it.matches(cc) }
        val maxspeedCountryCode = if (useSubdivisionCode) cc else cc.split("-").first()

        applyAnswer(DefaultMaxSpeed(maxspeedCountryCode, roadType, lit))
    }

    companion object {
        // TODO
        private var LAST_INPUT_SLOW_ZONE: Int? = null
    }
}
