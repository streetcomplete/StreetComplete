package de.westnordost.streetcomplete.quests.max_speed

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.quests.max_speed.SpeedType.*
import de.westnordost.streetcomplete.quests.max_speed.SpeedMeasurementUnit.*
import kotlinx.android.synthetic.main.quest_maxspeed.*
import java.lang.IllegalStateException


class AddMaxSpeedForm : AbstractQuestFormAnswerFragment<MaxSpeedAnswer>() {

    override val contentLayoutResId = R.layout.quest_maxspeed

    override val otherAnswers: List<OtherAnswer> get() {
        val result = mutableListOf<OtherAnswer>()

        val highwayTag = osmElement!!.tags["highway"]!!
        if (countryInfo.isLivingStreetKnown && MAYBE_LIVING_STREET.contains(highwayTag)) {
            result.add(OtherAnswer(R.string.quest_maxspeed_answer_living_street) { confirmLivingStreet() })
        }
        if (countryInfo.isAdvisorySpeedLimitKnown) {
            result.add(OtherAnswer(R.string.quest_maxspeed_answer_advisory_speed_limit) { switchToAdvisorySpeedLimit() })
        }
        return result
    }

    private var speedInput: EditText? = null
    private var speedUnitSelect: Spinner? = null
    private var speedType: SpeedType? = null

    private val speedUnits get() = countryInfo.speedUnits.map { it.toSpeedMeasurementUnit() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val couldBeSlowZone = countryInfo.isSlowZoneKnown && POSSIBLY_SLOWZONE_ROADS.contains(osmElement!!.tags["highway"]!!)
        zone.visibility = if (couldBeSlowZone) View.VISIBLE else View.GONE

        speedTypeSelect.setOnCheckedChangeListener { _, checkedId -> setSpeedType(getSpeedType(checkedId)) }

    }

    override fun onClickOk() {
        if (speedType == NO_SIGN) {
            val couldBeSlowZone = countryInfo.isSlowZoneKnown
                    && POSSIBLY_SLOWZONE_ROADS.contains(osmElement!!.tags["highway"])

            if (couldBeSlowZone)
                confirmNoSignSlowZone { determineImplicitMaxspeedType() }
            else
                confirmNoSign { determineImplicitMaxspeedType() }
        } else {
            if (userSelectedUnusualSpeed())
                confirmUnusualInput { applySpeedLimitFormAnswer() }
            else
                applySpeedLimitFormAnswer()
        }
    }

    override fun isFormComplete() = speedType == NO_SIGN || getSpeedFromInput() != null

    /* ---------------------------------------- With sign --------------------------------------- */

    private fun setSpeedType(speedType: SpeedType?) {
        this.speedType = speedType

        rightSideContainer.removeAllViews()
        speedType?.layoutResId?.let { layoutInflater.inflate(it, rightSideContainer, true) }

        speedInput = rightSideContainer.findViewById(R.id.maxSpeedInput)
        speedInput?.requestFocus()
        speedInput?.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })

        speedUnitSelect = rightSideContainer.findViewById(R.id.speedUnitSelect)
        speedUnitSelect?.visibility = if (speedUnits.size == 1) View.GONE else View.VISIBLE
        speedUnitSelect?.adapter = ArrayAdapter(context!!, R.layout.spinner_item_centered, speedUnits)
        speedUnitSelect?.setSelection(0)

        checkIsFormComplete()
    }

    private fun getSpeedType(@IdRes checkedId: Int) = when (checkedId) {
        R.id.sign ->    SIGN
        R.id.zone ->    ZONE
        R.id.no_sign -> NO_SIGN
        else -> null
    }

    private val SpeedType.layoutResId get() = when (this) {
        SIGN ->     R.layout.quest_maxspeed_sign
        ZONE ->     R.layout.quest_maxspeed_zone_sign
        ADVISORY -> R.layout.quest_maxspeed_advisory
        else -> null
    }

    private fun userSelectedUnusualSpeed(): Boolean {
        val speed = getSpeedFromInput() ?: return false
        val kmh = speed.toKmh()
        return kmh > 140 || kmh > 20 && speed.toValue() % 5 != 0
    }

    private fun switchToAdvisorySpeedLimit() {
        speedTypeSelect.clearCheck()
        for (i in 0 until speedTypeSelect.childCount) {
            speedTypeSelect.getChildAt(i).isEnabled = false
        }
        setSpeedType(ADVISORY)
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
            ADVISORY -> applyAnswer(AdvisorySpeedSign(speed))
            ZONE ->     applyAnswer(MaxSpeedZone(speed, countryInfo.countryCode, "zone${speed.toValue()}"))
            SIGN ->     applyAnswer(MaxSpeedSign(speed))
            else ->     throw IllegalStateException()
        }
    }

    private fun getSpeedFromInput(): SpeedMeasure? {
        val value = speedInput?.numberOrNull?.toInt() ?: return null
        val unit = speedUnitSelect?.selectedItem as SpeedMeasurementUnit? ?: speedUnits.first()
        return when(unit) {
            KILOMETERS_PER_HOUR -> Kmh(value)
            MILES_PER_HOUR -> Mph(value)
        }
    }

    /* ----------------------------------------- No sign ---------------------------------------- */

    // the living street answer stuff is copied to AddAccessibleForPedestriansForm
    private fun confirmLivingStreet() {
        activity?.let {
            val view = layoutInflater.inflate(R.layout.quest_maxspeed_living_street_confirmation, null, false)
            // this is necessary because the inflated image view uses the activity context rather than
            // the fragment / layout inflater context' resources to access it's drawable
            val img = view.findViewById<ImageView>(R.id.livingStreetImage)
            img.setImageDrawable(resources.getDrawable(R.drawable.ic_living_street))
            AlertDialog.Builder(it)
                .setView(view)
                .setTitle(R.string.quest_maxspeed_answer_living_street_confirmation_title)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsLivingStreet) }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

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
            val view = layoutInflater.inflate(R.layout.quest_maxspeed_no_sign_no_slow_zone_confirmation, null, false)
            val input = view.findViewById<EditText>(R.id.maxSpeedInput)
            input.setText("××")
            input.inputType = EditorInfo.TYPE_NULL

            AlertDialog.Builder(it)
                .setTitle(R.string.quest_maxspeed_answer_noSign_confirmation_title)
                .setView(view)
                .setPositiveButton(R.string.quest_maxspeed_answer_noSign_confirmation_positive) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        }
    }

    private fun determineImplicitMaxspeedType() {
        val highwayTag = osmElement!!.tags["highway"]!!
        if (ROADS_WITH_DEFINITE_SPEED_LIMIT.contains(highwayTag)) {
            applyNoSignAnswer(highwayTag)
        } else {
            if (countryInfo.countryCode == "GB") {
                determineLit(
                    onYes = { applyNoSignAnswer("nsl_restricted") },
                    onNo = {
                        askIsDualCarriageway(
                            onYes = { applyNoSignAnswer("nsl_dual") },
                            onNo = { applyNoSignAnswer("nsl_single") })
                    }
                )
            } else {
                askUrbanOrRural(
                    onUrban = { applyNoSignAnswer("urban") },
                    onRural = { applyNoSignAnswer("rural") })
            }
        }
    }

    private fun askUrbanOrRural(onUrban: () -> Unit, onRural: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.quest_maxspeed_answer_noSign_info_urbanOrRural)
                .setMessage(R.string.quest_maxspeed_answer_noSign_urbanOrRural_description)
                .setPositiveButton(R.string.quest_maxspeed_answer_noSign_urbanOk) { _, _ -> onUrban() }
                .setNegativeButton(R.string.quest_maxspeed_answer_noSign_ruralOk) { _, _ -> onRural() }
                .show()
        }
    }

    private fun determineLit(onYes: () -> Unit, onNo: () -> Unit) {
        val lit = osmElement!!.tags["lit"]
        when (lit) {
            "yes" -> onYes()
            "no" -> onNo()
            else -> askLit(onYes, onNo)
        }
    }

    private fun askLit(onYes: () -> Unit, onNo: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.quest_way_lit_road_title)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> onYes() }
                .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> onNo() }
                .show()
        }
    }

    private fun askIsDualCarriageway(onYes: () -> Unit, onNo: () -> Unit) {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.quest_maxspeed_answer_noSign_singleOrDualCarriageway_description)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> onYes() }
                .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> onNo() }
                .show()
        }
    }

    private fun applyNoSignAnswer(roadType: String) {
        applyAnswer(ImplicitMaxSpeed(countryInfo.countryCode, roadType))
    }

    companion object {
        private val POSSIBLY_SLOWZONE_ROADS = listOf("residential", "unclassified", "tertiary" /*#1133*/)
        private val MAYBE_LIVING_STREET = listOf("residential", "unclassified")
        private val ROADS_WITH_DEFINITE_SPEED_LIMIT = listOf("trunk", "motorway", "living_street")
    }
}

private enum class SpeedType {
    SIGN, ZONE, ADVISORY, NO_SIGN
}
