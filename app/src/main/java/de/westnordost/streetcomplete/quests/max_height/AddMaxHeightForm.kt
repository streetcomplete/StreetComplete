package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.controller.LengthInputViewController

class AddMaxHeightForm : AbstractOsmQuestForm<MaxHeightAnswer>() {

    private lateinit var lengthInput: LengthInputViewController

    override val contentLayoutResId get() = when (countryInfo.countryCode) {
        "AU", "NZ", "US", "CA" -> R.layout.quest_maxheight_mutcd
        else -> R.layout.quest_maxheight
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxheight_answer_noSign) { confirmNoSign() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View?>(R.id.meterInputSign)
            ?.setBackgroundResource(getSignBackgroundDrawableResId(countryInfo.countryCode))
        view.findViewById<View?>(R.id.feetInputSign)
            ?.setBackgroundResource(getSignBackgroundDrawableResId(countryInfo.countryCode))

        val splitWayHint = view.findViewById<TextView>(R.id.splitWayHint)
        splitWayHint?.text = getString(R.string.quest_maxheight_split_way_hint, getString(R.string.quest_generic_answer_differs_along_the_way))
        splitWayHint?.isGone = element.type == ElementType.NODE

        lengthInput = LengthInputViewController(
            unitSelect = view.findViewById(R.id.heightUnitSelect),
            metersContainer = view.findViewById(R.id.meterInputSign),
            metersInput = view.findViewById(R.id.meterInput),
            feetInchesContainer = view.findViewById(R.id.feetInputSign),
            feetInput = view.findViewById(R.id.feetInput),
            inchesInput = view.findViewById(R.id.inchInput)
        )
        lengthInput.maxFeetDigits = 2
        lengthInput.maxMeterDigits = Pair(2, 2)
        lengthInput.selectableUnits = countryInfo.lengthUnits
        lengthInput.onInputChanged = { checkIsFormComplete() }
    }

    override fun isFormComplete() = lengthInput.length != null

    override fun onClickOk() {
        if (userSelectedUnrealisticHeight()) {
            confirmUnusualInput { applyMaxHeightFormAnswer() }
        } else {
            applyMaxHeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticHeight(): Boolean {
        val m = lengthInput.length?.toMeters() ?: return false
        return m > 6 || m < 1.8
    }

    private fun applyMaxHeightFormAnswer() {
        applyAnswer(MaxHeight(lengthInput.length!!))
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_maxheight_answer_noSign_question)
            .setPositiveButton(R.string.quest_maxheight_answer_noSign_question_yes) { _, _ -> applyAnswer(NoMaxHeightSign(true)) }
            .setNegativeButton(R.string.quest_maxheight_answer_noSign_question_no) { _, _ -> applyAnswer(NoMaxHeightSign(false)) }
            .show()
        }
    }

    private fun confirmUnusualInput(callback: () -> (Unit)) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}

private fun getSignBackgroundDrawableResId(countryCode: String): Int = when (countryCode) {
    "FI", "IS", "SE" -> R.drawable.background_maxheight_sign_yellow
    else ->             R.drawable.background_maxheight_sign
}
