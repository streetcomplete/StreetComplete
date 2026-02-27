package de.westnordost.streetcomplete.quests.max_speed

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.databinding.QuestMaxspeedNoSignNoSlowZoneConfirmationBinding
import de.westnordost.streetcomplete.osm.maxspeed.COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddMaxSpeedForm : AbstractOsmQuestForm<MaxSpeedAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    override val otherAnswers: List<AnswerItem> get() {

        val result = mutableListOf<AnswerItem>()
        /*
        if (countryInfo.hasAdvisorySpeedLimitSign) {
            result.add(AnswerItem(R.string.quest_maxspeed_answer_advisory_speed_limit) { switchToAdvisorySpeedLimit() })
        }
        */
        return result
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val highwayTag = element.tags["highway"]!!

        val couldBeSlowZone = countryInfo.hasSlowZone && ROADS_WHERE_SLOW_ZONE_IS_POSSIBLE.contains(highwayTag)
        binding.zone.isGone = !couldBeSlowZone

        val couldBeLivingStreet = countryInfo.hasLivingStreet && MAYBE_LIVING_STREET.contains(highwayTag)
        binding.livingStreet.isGone = !couldBeLivingStreet
    }

    override fun onClickOk() {
        if (speedType == NO_SIGN) {
            val slowZoneLikely = countryInfo.hasSlowZone && element.tags["highway"] == "residential"

            if (slowZoneLikely) {
                confirmNoSignSlowZone { determineImplicitMaxspeedType() }
            } else {
                confirmNoSign { determineImplicitMaxspeedType() }
            }
        } else if (speedType == LIVING_STREET) {
            applyAnswer(IsLivingStreet)
        } else if (speedType == NSL) {
            askIsDualCarriageway(
                onYes = { applyNoSignAnswer("nsl_dual") },
                onNo = { applyNoSignAnswer("nsl_single") }
            )
        } else if (userSelectedUnusualSpeed()) {
            confirmUnusualInput { applySpeedLimitFormAnswer() }
        } else {
            applySpeedLimitFormAnswer()
        }
    }

    override fun isFormComplete() =
        speedType == NO_SIGN || speedType == LIVING_STREET || speedType == NSL || getSpeedFromInput() != null

    /* ---------------------------------------- With sign --------------------------------------- */

    private fun userSelectedUnusualSpeed(): Boolean {
        val speed = getSpeedFromInput() ?: return false
        val isDividableByFive = speed.toValue() % 5 == 0
        val kmh = speed.toKmh()
        return when (speedType) {
            SIGN -> kmh > 140 || kmh > 20 && !isDividableByFive || kmh < 5
            ZONE -> kmh > 40 || kmh > 20 && !isDividableByFive || kmh < 5
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
        val speed = getSpeedFromInput()!!
        when (speedType) {
            ADVISORY      -> applyAnswer(AdvisorySpeedSign(speed))
            ZONE          -> {
                val zoneX = speed.toValue()
                LAST_INPUT_SLOW_ZONE = zoneX
                applyAnswer(MaxSpeedZone(speed, countryInfo.countryCode, "zone$zoneX"))
            }
            SIGN          -> applyAnswer(MaxSpeedSign(speed))
            else          -> throw IllegalStateException()
        }
    }

*/
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
        // i.e. where to offer the option to select it. See also #5771, 1133
        // - sometimes also main roads have that sign
        private val ROADS_WHERE_SLOW_ZONE_IS_POSSIBLE = listOf(
            "residential", "unclassified",
            "tertiary", "tertiary_link", "secondary", "secondary_link", "primary", "primary_link"
        )
        private val MAYBE_LIVING_STREET = listOf("residential", "unclassified")

        private var LAST_INPUT_SLOW_ZONE: Int? = null
    }
}
